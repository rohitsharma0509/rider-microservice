package com.scb.rider.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;
import com.scb.rider.service.document.RiderProfileService;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import com.scb.rider.service.factory.BackgroundVerificationUploadService;
import com.scb.rider.service.factory.RiderDocumentFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderUploadedDocumentServiceTest {

  private static final String PROFILE_ID =  "12345";
  private static final String FILE_NAME = "hello.png";
  private static final String NEW_NAME = "new.png";

  @Mock
  private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

  @Mock
  private AmazonS3ImageService s3ImageService;

  @InjectMocks
  private RiderUploadedDocumentService uploadedDocumentService;

  @Mock
  private RiderProfileRepository riderProfileRepository;

  @Mock
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @Mock
  private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

  @Mock
  private RiderDocumentFactory riderDocumentFactory;

  @Mock
  private BackgroundVerificationUploadService backgroundVerificationUploadService;
  
  @Mock
  private RiderProfileService riderProfileService;

  @Test
  public void uploadedDocumentNotAvailable()
      throws InvalidImageExtensionException, FileConversionException {

    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(riderProfileRepository.findById(profileId)).thenReturn(Optional.of(new RiderProfile()));

    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("s3-image-url");

    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
        .thenReturn(Optional.of(uploadedDocument));
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class)))
        .thenReturn(uploadedDocument);


    RiderUploadedDocument response = uploadedDocumentService.uploadedDocument(mockMultipartFile,
        "12345", DocumentType.VEHICLE_REGISTRATION, Constants.OPS_MEMBER, FoodBoxSize.SMALL.name());

    assertEquals("12345", response.getRiderProfileId(), "Invalid Response");

  }

  @Test
  public void uploadedDocumentWhenRiderIdNotAvailable()
      throws InvalidImageExtensionException, FileConversionException {

    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("s3-image-url");

    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
        .thenReturn(Optional.empty());
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class)))
        .thenReturn(uploadedDocument);

    assertThrows(DataNotFoundException.class, () -> uploadedDocumentService
        .uploadedDocument(mockMultipartFile, "12345", DocumentType.VEHICLE_REGISTRATION, Constants.OPS_MEMBER, FoodBoxSize.SMALL.name()));

  }

  @Test
  public void uploadedDocumentAlreadyAvailable()
      throws InvalidImageExtensionException, FileConversionException {

    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(riderProfileRepository.findById(profileId)).thenReturn(Optional.of(new RiderProfile()));
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(any(), eq(TrainingType.FOOD))).thenReturn(Optional.empty());
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("s3-image-url");

    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
        .thenReturn(Optional.empty());
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class)))
        .thenReturn(uploadedDocument);
    when(riderProfileRepository.findById(Mockito.any(String.class)))
        .thenReturn(Optional.of(new RiderProfile()));

    RiderUploadedDocument response = uploadedDocumentService.uploadedDocument(mockMultipartFile,
        "12345", DocumentType.VEHICLE_REGISTRATION, Constants.OPS_MEMBER, FoodBoxSize.SMALL.name());

    assertEquals("12345", response.getRiderProfileId(), "Invalid Response");

  }

  @Test
  public void uploadedDocumentAndUpdateProfilePhotoStatusToPending() throws InvalidImageExtensionException, FileConversionException {
    when(riderProfileRepository.findById("12345")).thenReturn(Optional.of(new RiderProfile()));
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any())).thenReturn("s3-image-url");
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType("12345", DocumentType.PROFILE_PHOTO)).thenReturn(Optional.empty());
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(any(), eq(TrainingType.FOOD))).thenReturn(Optional.empty());
    RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345")
            .imageUrl("url").documentType(DocumentType.PROFILE_PHOTO).build();
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class))).thenReturn(uploadedDocument);
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    RiderUploadedDocument response = uploadedDocumentService.uploadedDocument(mockMultipartFile, "12345", DocumentType.PROFILE_PHOTO
            , Constants.OPS_MEMBER, FoodBoxSize.SMALL.name());
    assertEquals("12345", response.getRiderProfileId(), "Invalid Response");
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  public void uploadedDocumentAndUpdateFoodCart() throws InvalidImageExtensionException, FileConversionException {
    when(riderProfileRepository.findById("12345")).thenReturn(Optional.of(new RiderProfile()));
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any())).thenReturn("s3-image-url");
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType("12345", DocumentType.VEHICLE_WITH_FOOD_CARD)).thenReturn(Optional.empty());
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(any(), eq(TrainingType.FOOD))).thenReturn(Optional.empty());
    RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345")
            .imageUrl("url").documentType(DocumentType.VEHICLE_WITH_FOOD_CARD).build();
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class))).thenReturn(uploadedDocument);
    RiderVehicleRegistrationDocument vehicleRegistrationDocument = RiderVehicleRegistrationDocument.builder()
            .riderProfileId("12345").status(MandatoryCheckStatus.PENDING).build();
    when(vehicleRegistrationRepository.findByRiderProfileId("12345")).thenReturn(Optional.of(vehicleRegistrationDocument));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    Mockito.doNothing().when(riderProfileService).documentUploadedFlag(anyString(), any());
    RiderUploadedDocument response = uploadedDocumentService.uploadedDocument(mockMultipartFile, "12345", DocumentType.VEHICLE_WITH_FOOD_CARD
            , Constants.OPS_MEMBER, FoodBoxSize.SMALL.name());
    assertEquals("12345", response.getRiderProfileId(), "Invalid Response");
    verify(vehicleRegistrationRepository, times(1)).save(any(RiderVehicleRegistrationDocument.class));
  }

  @Test
  public void fetchDocumentByProfileIdAndDocType()
      throws InvalidImageExtensionException, FileConversionException {

    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("s3-image-url");

    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
        .thenReturn(Optional.of(uploadedDocument));


    Optional<RiderUploadedDocument> response =
        uploadedDocumentService.fetchDocument("12345", DocumentType.VEHICLE_REGISTRATION);

    assertEquals("12345", response.get().getRiderProfileId(), "Invalid Response");

  }

  @Test
  public void fetchDocumentByProfileIdAndDocTypeNotFound()
      throws InvalidImageExtensionException, FileConversionException {

    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("s3-image-url");

    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
        .thenReturn(Optional.of(uploadedDocument));


    Optional<RiderUploadedDocument> response =
        uploadedDocumentService.fetchDocument("12345", DocumentType.VEHICLE_REGISTRATION);

    assertEquals("12345", response.get().getRiderProfileId(), "Invalid Response");

  }

  @Test
  public void uploadedDocumentWithProfileMarkAsStage3() throws InvalidImageExtensionException, FileConversionException {
    String profileId = "12345";
    when(riderProfileRepository.findById(profileId)).thenReturn(Optional.of(new RiderProfile()));
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(s3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
            .thenReturn("s3-image-url");
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, DocumentType.BACKGROUND_VERIFICATION_FORM))
            .thenReturn(Optional.empty());
    RiderSelectedTrainingAppointment riderTraining = RiderSelectedTrainingAppointment.builder()
            .status(RiderTrainingStatus.COMPLETED).build();
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(any(), eq(TrainingType.FOOD))).thenReturn(Optional.of(riderTraining));
    RiderUploadedDocument response = uploadedDocumentService.uploadedDocument(mockMultipartFile,
            profileId, DocumentType.BACKGROUND_VERIFICATION_FORM, Constants.OPS_MEMBER, FoodBoxSize.SMALL.name());
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  public void downloadDocumentByProfileIdAndFileName() throws IOException {
    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(s3ImageService.downloadFile("12345", "1609864079957-sample.jpg"))
            .thenReturn(mockMultipartFile.getBytes());
    byte[] response = uploadedDocumentService.downloadDocument("12345", "1609864079957-sample.jpg");
    assertTrue(ObjectUtils.isNotEmpty(response));
  }

  @Test
  public void downloadDocumentFileName() {
    String profileId = "12345";
    DocumentType docType = DocumentType.VEHICLE_REGISTRATION;
    RiderUploadedDocument uploadedDocument =
            RiderUploadedDocument.builder().id("primaryKey").riderProfileId("12345").imageUrl("url")
                    .documentType(DocumentType.VEHICLE_REGISTRATION).build();
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(profileId, docType))
            .thenReturn(Optional.of(uploadedDocument));
    String response = uploadedDocumentService.getDownloadFileName(profileId, docType);
    assertTrue(ObjectUtils.isNotEmpty(response));
  }

  @Test
  public void uploadMultipleDocumentsShouldThrowExceptionWhenRiderNotExist() {
    when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.empty());
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    assertThrows(DataNotFoundException.class, () -> uploadedDocumentService.uploadMultipleDocument(PROFILE_ID,
            DocumentType.BACKGROUND_VERIFICATION_FORM, new MultipartFile[]{mockMultipartFile}, Constants.OPS_MEMBER, Boolean.TRUE));
  }

  @Test
  public void uploadMultipleDocumentsShouldUploadToS3WhenDocumentNotAlreadyExists() throws InvalidImageExtensionException, IOException {
    when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(new RiderProfile()));
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID),
            eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(Optional.empty());
    when(riderDocumentFactory.getInstance(eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(backgroundVerificationUploadService);
    when(s3ImageService.uploadMultipleFiles(any(MultipartFile[].class), eq(PROFILE_ID))).thenReturn(Arrays.asList(FILE_NAME));
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class))).thenReturn(getRiderUploadedDocument());
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    RiderUploadedDocument result = uploadedDocumentService.uploadMultipleDocument(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM,
            new MultipartFile[]{mockMultipartFile}, Constants.OPS_MEMBER, Boolean.TRUE);
    verify(backgroundVerificationUploadService, times(1)).performOperation(any(RiderProfile.class));
    assertEquals(PROFILE_ID, result.getRiderProfileId());
    assertEquals(DocumentType.BACKGROUND_VERIFICATION_FORM, result.getDocumentType());
    assertTrue(result.getDocumentUrls().contains(FILE_NAME));
  }

  @Test
  public void uploadMultipleDocumentsShouldRemoveAndUploadToS3WhenDocumentAlreadyExists() throws InvalidImageExtensionException, IOException {
    when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(new RiderProfile()));
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID),
            eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(Optional.of(getRiderUploadedDocument()));
    when(riderDocumentFactory.getInstance(eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(backgroundVerificationUploadService);
    when(s3ImageService.uploadMultipleFiles(any(MultipartFile[].class), eq(PROFILE_ID))).thenReturn(Arrays.asList(FILE_NAME));
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class))).thenReturn(getRiderUploadedDocument());
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    RiderUploadedDocument result = uploadedDocumentService.uploadMultipleDocument(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM,
            new MultipartFile[]{mockMultipartFile}, Constants.OPS_MEMBER, Boolean.TRUE);
    verify(backgroundVerificationUploadService, times(1)).performOperation(any(RiderProfile.class));
    verify(s3ImageService, times(1)).removeMultipleFiles(anyList(), eq(PROFILE_ID));
    assertEquals(PROFILE_ID, result.getRiderProfileId());
    assertEquals(DocumentType.BACKGROUND_VERIFICATION_FORM, result.getDocumentType());
    assertTrue(result.getDocumentUrls().contains(FILE_NAME));
  }

  @Test
  public void uploadMultipleDocumentsShouldNotRemoveFromS3WhenReplaceFlagIsFalse() throws InvalidImageExtensionException, IOException {
    when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(new RiderProfile()));
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID),
            eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(Optional.of(getRiderUploadedDocument()));
    when(riderDocumentFactory.getInstance(eq(DocumentType.BACKGROUND_VERIFICATION_FORM))).thenReturn(backgroundVerificationUploadService);
    when(s3ImageService.uploadMultipleFiles(any(MultipartFile[].class), eq(PROFILE_ID))).thenReturn(Arrays.asList(FILE_NAME));
    when(riderUploadedDocumentRepository.save(any(RiderUploadedDocument.class))).thenReturn(getRiderUploadedDocument());
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    RiderUploadedDocument result = uploadedDocumentService.uploadMultipleDocument(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM,
            new MultipartFile[]{mockMultipartFile}, Constants.OPS_MEMBER, Boolean.FALSE);
    verify(backgroundVerificationUploadService, times(1)).performOperation(any(RiderProfile.class));
    verify(s3ImageService, times(0)).removeMultipleFiles(anyList(), eq(PROFILE_ID));
    assertEquals(PROFILE_ID, result.getRiderProfileId());
    assertEquals(DocumentType.BACKGROUND_VERIFICATION_FORM, result.getDocumentType());
    assertTrue(result.getDocumentUrls().contains(FILE_NAME));
  }

  @Test
  public void deleteAndSaveDocumentsTestWhenDoNotHaveDocumentInRequest() {
    uploadedDocumentService.deleteAndSaveDocuments(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM, null);
    verifyZeroInteractions(s3ImageService);
  }

  @Test
  public void deleteAndSaveDocumentsTestWhenExistingDocumentIsNull() {
    RiderUploadedDocument riderUploadedDocument = RiderUploadedDocument.builder().riderProfileId(PROFILE_ID)
            .documentType(DocumentType.BACKGROUND_VERIFICATION_FORM).build();
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID), eq(DocumentType.BACKGROUND_VERIFICATION_FORM)))
            .thenReturn(Optional.of(riderUploadedDocument));
    uploadedDocumentService.deleteAndSaveDocuments(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM, Arrays.asList(FILE_NAME));
    verifyZeroInteractions(s3ImageService);
  }

  @Test
  public void deleteAndSaveDocumentsTestWhenDoNotHaveDocumentToRemove() {
    RiderUploadedDocument riderUploadedDocument = RiderUploadedDocument.builder().riderProfileId(PROFILE_ID)
            .documentUrls(Arrays.asList(FILE_NAME)).documentType(DocumentType.BACKGROUND_VERIFICATION_FORM).build();
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID), eq(DocumentType.BACKGROUND_VERIFICATION_FORM)))
            .thenReturn(Optional.of(riderUploadedDocument));
    uploadedDocumentService.deleteAndSaveDocuments(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM, Arrays.asList(FILE_NAME));
    verifyZeroInteractions(s3ImageService);
  }

  @Test
  public void deleteAndSaveDocumentsTestWhenHaveDocumentToRemove() {
    RiderUploadedDocument riderUploadedDocument = RiderUploadedDocument.builder().riderProfileId(PROFILE_ID)
            .documentUrls(Arrays.asList(FILE_NAME, NEW_NAME)).documentType(DocumentType.BACKGROUND_VERIFICATION_FORM).build();
    when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID), eq(DocumentType.BACKGROUND_VERIFICATION_FORM)))
            .thenReturn(Optional.of(riderUploadedDocument));
    uploadedDocumentService.deleteAndSaveDocuments(PROFILE_ID, DocumentType.BACKGROUND_VERIFICATION_FORM, Arrays.asList(NEW_NAME));
    verify(s3ImageService, times(1)).removeMultipleFiles(anyList(), eq(PROFILE_ID));
    verify(riderUploadedDocumentRepository, times(1)).save(any(RiderUploadedDocument.class));
  }

  private RiderUploadedDocument getRiderUploadedDocument() {
    return RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentUrls(Arrays.asList(FILE_NAME)).documentType(DocumentType.BACKGROUND_VERIFICATION_FORM).build();
  }
}
