package com.scb.rider.service;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.enumeration.RiderFileFormats;
import com.scb.rider.util.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Service
@SuppressWarnings("squid:S4790")
public class AmazonS3ImageService extends AmazonClientService {

  private static final String RIDER_DOCUMENTS_FOLDER = "rider-documents";

  private static final String RIDER_PROFILE_PIC_FOLDER = "rider-documents/profile/";

  public String uploadMultipartFile(MultipartFile multipartFile, String riderId, DocumentType doctype)
      throws FileConversionException, InvalidImageExtensionException {

    RiderFileFormats.validateFileExtension(multipartFile);
    String fileUrl;
    try {
      File file = FileUtils.convertMultipartToFile(multipartFile);
      String fileName = FileUtils.generateFileName(multipartFile);
      uploadPublicFile(fileName, file, riderId, doctype);
      if (!Files.deleteIfExists(file.toPath())) {
        log.info("Unable to delete file: ", fileName);
      }
      fileUrl = fileName;
    } catch (IOException e) {
      throw new FileConversionException(e);
    }
    return fileUrl;
  }

  private void uploadPublicFile(String fileName, File file, String riderId, DocumentType docType)
          throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setContentLength(file.length());
      objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
      PutObjectRequest putRequest = new PutObjectRequest(getBucketName(), getKey(fileName, riderId),
              is, objectMetadata);

      getClient().putObject(putRequest);
      if (DocumentType.getPublicDocumentTypeList().contains(docType.name())) {
        uploadPublicDocumentsOnExternalBucket(fileName, file, riderId, docType, objectMetadata);
      }
    }
  }

  public void uploadPublicDocumentsOnExternalBucket(String fileName, File file,
      String riderId, DocumentType docType, ObjectMetadata objectMetadata) throws FileNotFoundException {
    log.info(String.format("Upload Public File for riderId-%s doctype-%s", riderId, docType.name()));

    boolean isObjectExists = getClient().doesObjectExist(getExternalBucketName(),
        getPublicS3Key(fileName, riderId));
    if(isObjectExists) {
      log.info("File  Exists in External Bucket riderId-%s doctype-%s", riderId, docType.name());
      deletePublicDocumentsOnExternalBucket(fileName, riderId, docType);
    }
    AccessControlList acl = new AccessControlList();
    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    //NOTE Uploading to External Bucket

    PutObjectRequest putRequest = new PutObjectRequest(getExternalBucketName(), getPublicS3Key(fileName, riderId),
        new FileInputStream(file), objectMetadata);
    putRequest.setAccessControlList(acl);
    getClient().putObject(putRequest);

  }

  private String getPublicS3Key(String fileName, String riderId) {
    String fileExtension = FilenameUtils.getExtension(fileName);
    StringBuilder key = new StringBuilder();
    try {
      StringBuffer riderIdString = new StringBuffer();
      //NOSONAR
      MessageDigest md = MessageDigest.getInstance("SHA-256");

      byte[] digest = md.digest(riderId.getBytes(StandardCharsets.UTF_8));

      for (int i = 0; i<digest.length; i++) {
        riderIdString.append(Integer.toHexString(0xFF & digest[i]));
      }

      key.append(RIDER_PROFILE_PIC_FOLDER).append(riderIdString).append(".").append(fileExtension);
    }
    catch(NoSuchAlgorithmException ex) {
      log.error("NoSuchAlgorithmException  Occured");
      key.append(RIDER_PROFILE_PIC_FOLDER).append(riderId.getBytes(StandardCharsets.UTF_8)).append(".").append(fileExtension);
    }
    return key.toString();
  }

  public List<String> uploadMultipleFiles(MultipartFile[] multipartFiles, String riderId)
          throws InvalidImageExtensionException, IOException {
    RiderFileFormats.validateFileExtension(multipartFiles);
    List<String> uploadedFiles = new ArrayList<>();
    for(MultipartFile multipartFile: multipartFiles) {
      String fileName = FileUtils.generateFileName(multipartFile);
      uploadInputStream(multipartFile.getInputStream(), RIDER_DOCUMENTS_FOLDER, riderId, fileName);
      uploadedFiles.add(fileName);
    }
    return uploadedFiles;
  }

  public void removeMultipleFiles(List<String> files, String riderId) {
    log.info("deleting multiple files {} from aws bucket", files);
    List<KeyVersion> keys = files.stream().map(file -> new KeyVersion(getKey(file, riderId))).collect(Collectors.toList());
    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(getBucketName()).withKeys(keys);
    getClient().deleteObjects(deleteObjectsRequest);
    log.info("files deleted successfully");
  }

  public void removeImageFromAmazon(String fileName, String riderId, DocumentType docType) {
    DeleteObjectRequest deleteObjectRequest =
        new DeleteObjectRequest(getBucketName(), getKey(fileName, riderId));
    getClient().deleteObject(deleteObjectRequest);
  }

  public void deletePublicDocumentsOnExternalBucket(String fileName, String riderId, DocumentType docType) {
    log.info(String.format("Deleting Public File for riderId-%s doctype-%s", riderId, docType.name()));
    DeleteObjectRequest deleteObjectRequest =
        new DeleteObjectRequest(getExternalBucketName(), getPublicS3Key(fileName, riderId));
    getClient().deleteObject(deleteObjectRequest);
  }



  public byte[] downloadFile(String riderId, String fileName) throws IOException {

    GetObjectRequest getObjectRequest =
        new GetObjectRequest(getBucketName(), getKey(fileName, riderId));
    S3Object object = getClient().getObject(getObjectRequest);
    return IOUtils.toByteArray(object.getObjectContent());
  }

  private String getKey(String fileName, String riderId) {
    return RIDER_DOCUMENTS_FOLDER + "/" + riderId + "/" + fileName;
  }



  public String getPublicDocumentUrl(String fileName, String riderId) {
    return getClient().getUrl(getExternalBucketName(), getPublicS3Key(fileName, riderId)).toString();
  }
  public String uploadFile(File file, String folderName, String key) throws IOException {
    try (InputStream inputStream = new FileInputStream(file)) {
      return uploadInputStream(inputStream, folderName, key, file.getName());
    }
  }

  public String uploadInputStream(InputStream fileStream, String folderName, String key, String fileName) throws IOException {
    log.info("bucketName {}, folderName {}, key {}, fileName {}", getBucketName(), folderName, key, fileName);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(fileStream.available());
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    PutObjectRequest putRequest = new PutObjectRequest(getBucketName(), getKey(folderName, key, fileName), fileStream, objectMetadata);
    getClient().putObject(putRequest);
    log.info("File {} has been uploaded successfully on s3", fileName);
    return fileName;
  }

  public byte[] downloadFile(String folderName, String key, String fileName) throws IOException {
    log.info("Downloading bucketName {}, folderName {}, key {}, fileName {}", getBucketName(), folderName, key, fileName);
    GetObjectRequest getObjectRequest = new GetObjectRequest(getBucketName(), getKey(folderName, key, fileName));
    S3Object object = getClient().getObject(getObjectRequest);
    log.info("File {} has been downloaded successfully on s3", object.getKey());
    return IOUtils.toByteArray(object.getObjectContent());
  }

  private String getKey(String folderName, String key, String fileName) {
    StringBuilder sb = new StringBuilder(folderName);
    sb.append("/").append(key).append("/").append(fileName);
    return sb.toString();
  }

}
