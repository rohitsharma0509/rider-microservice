package com.scb.rider.service.document;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.enumeration.*;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.service.factory.RiderDocumentFactory;
import com.scb.rider.service.factory.UploadService;
import com.scb.rider.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.*;

@Service
@Log4j2
public class RiderUploadedDocumentService {

  @Autowired
  private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

  @Autowired
  private RiderProfileRepository riderProfileRepository;

  @Autowired
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @Autowired
  private AmazonS3ImageService s3ImageService;

  @Autowired
  private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

  @Autowired
  private RiderDocumentFactory riderDocumentFactory;
  
  @Autowired
  private RiderProfileService riderProfileService;

  public RiderUploadedDocument uploadedDocument(MultipartFile file, String profileId,
      DocumentType docType, String userId, String foodBoxSize) throws InvalidImageExtensionException, FileConversionException {
      RiderProfile riderProfile = riderProfileRepository.findById(profileId).orElseThrow(
          () -> LoggerUtils.logError(RiderUploadedDocument.class, profileId, "profileId"));

    String imageUrl = s3ImageService.uploadMultipartFile(file, profileId, docType);
    RiderUploadedDocument uploadedDocument = null;
    Optional<RiderUploadedDocument> riderUploadedDocument =
        riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType);

    if (riderUploadedDocument.isPresent()) {
      s3ImageService.removeImageFromAmazon(riderUploadedDocument.get().getImageUrl(), profileId, docType);
      uploadedDocument = riderUploadedDocument.get();
      uploadedDocument.setDocumentType(docType);
      uploadedDocument.setImageUrl(imageUrl);
      uploadedDocument.setRiderProfileId(profileId);
    } else {
      Optional<RiderSelectedTrainingAppointment> riderTraining = riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD);
      if (DocumentType.BACKGROUND_VERIFICATION_FORM.equals(docType)) {
        if(riderTraining.isPresent() && RiderTrainingStatus.COMPLETED.equals(riderTraining.get().getStatus())) {
          riderProfile.setProfileStage(RiderProfileStage.STAGE_3);
          riderProfileRepository.save(riderProfile);
        }
      }
      uploadedDocument = RiderUploadedDocument.builder().documentType(docType).imageUrl(imageUrl)
          .riderProfileId(profileId).build();
    }

    if(DocumentType.PROFILE_PHOTO.equals(docType)) {
      riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
      riderProfileRepository.save(riderProfile);
    } else if(DocumentType.VEHICLE_WITH_FOOD_CARD.equals(docType)) {
      Optional<RiderVehicleRegistrationDocument> document = vehicleRegistrationRepository.findByRiderProfileId(profileId);
      if(document.isPresent()) {
        RiderVehicleRegistrationDocument vehicleRegistrationDocument = document.get();
        vehicleRegistrationDocument.setUploadedFoodCardUrl(imageUrl);
        vehicleRegistrationDocument.setFoodCardStatus(MandatoryCheckStatus.PENDING);
        if(StringUtils.isNotBlank(foodBoxSize)) {
          vehicleRegistrationDocument.setFoodBoxSize(FoodBoxSize.valueOf(foodBoxSize));
        }
        vehicleRegistrationRepository.save(vehicleRegistrationDocument);
      }
    }
    
    if(DocumentType.getPublicDocumentTypeList().contains(docType.name())) {
      uploadedDocument.setImageExternalUrl(s3ImageService.getPublicDocumentUrl(imageUrl, profileId));
    }
    uploadedDocument.setDocumentUrls(Arrays.asList(uploadedDocument.getImageUrl()));
    uploadedDocument.setUpdatedBy(userId);

    riderProfileService.documentUploadedFlag(profileId, docType);
    return riderUploadedDocumentRepository.save(uploadedDocument);
  }

  public Optional<RiderUploadedDocument> fetchDocument(String profileId, DocumentType docType) {
    return riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType);
  }
  
  public String getDownloadFileName(@NotEmpty String id, DocumentType documentType) {
    String documentKey= "";
    if(!ObjectUtils.isEmpty(documentType)) {
      Optional<RiderUploadedDocument> riderUploadDocument = fetchDocument(id, documentType);
      if(riderUploadDocument.isPresent() && !StringUtils.isEmpty(riderUploadDocument.get().getImageUrl())) {
        documentKey = riderUploadDocument.get().getImageUrl().toString();
      }
    }
    return documentKey;
  }

  public byte[] downloadDocument(@NotEmpty String id, String documentKey) throws IOException {
    return s3ImageService.downloadFile(id, documentKey);
  }

  public RiderUploadedDocument uploadMultipleDocument(String profileId, DocumentType documentType, MultipartFile[] files, String userId, Boolean replaceExisting)
          throws InvalidImageExtensionException, IOException {
    RiderProfile riderProfile = riderProfileRepository.findById(profileId).orElseThrow(() -> LoggerUtils.logError(RiderUploadedDocument.class, profileId, "profileId"));
    Optional<RiderUploadedDocument> riderUploadedDocument = riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, documentType);

    log.info("perform document specific operations with profileId {}, replaceExisting {}", profileId, replaceExisting);
    UploadService uploadService = riderDocumentFactory.getInstance(documentType);
    if (Objects.nonNull(uploadService)) uploadService.performOperation(riderProfile);

    List<String> newUploadedDocuments = s3ImageService.uploadMultipleFiles(files, profileId);

    RiderUploadedDocument uploadedDocument;
    if (riderUploadedDocument.isPresent()) {
      uploadedDocument = riderUploadedDocument.get();
      List<String> existingDocuments = uploadedDocument.getDocumentUrls();
      if (replaceExisting) {
        s3ImageService.removeMultipleFiles(existingDocuments, profileId);
        uploadedDocument.setDocumentUrls(newUploadedDocuments);
      } else {
        List<String> allDocuments = new ArrayList<>(existingDocuments);
        allDocuments.addAll(newUploadedDocuments);
        uploadedDocument.setDocumentUrls(allDocuments);
      }
    } else {
      uploadedDocument = RiderUploadedDocument.builder().documentType(documentType).documentUrls(newUploadedDocuments)
              .riderProfileId(profileId).build();
    }
    uploadedDocument.setImageUrl(!CollectionUtils.isEmpty(newUploadedDocuments) ? newUploadedDocuments.get(0) : null);
    uploadedDocument.setUpdatedBy(userId);
    riderProfileService.documentUploadedFlag(profileId, documentType);
    return riderUploadedDocumentRepository.save(uploadedDocument);
  }

  public void deleteAndSaveDocuments(String profileId, DocumentType docType, List<String> documentsInRequest) {
    if(!CollectionUtils.isEmpty(documentsInRequest)) {
      Optional<RiderUploadedDocument> uploadedDocuments = riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType);
      if (uploadedDocuments.isPresent()) {
        RiderUploadedDocument riderUploadedDocument = uploadedDocuments.get();
        List<String> existingDocuments = riderUploadedDocument.getDocumentUrls();
        if(CollectionUtils.isEmpty(existingDocuments)) {
          return;
        }
        List<String> allDocuments = new ArrayList<>(existingDocuments);
        allDocuments.removeAll(documentsInRequest);
        if (!CollectionUtils.isEmpty(allDocuments)) {
          s3ImageService.removeMultipleFiles(allDocuments, profileId);
          riderUploadedDocument.setDocumentUrls(documentsInRequest);
          riderUploadedDocumentRepository.save(riderUploadedDocument);
        }
      }
    }
  }

}
 