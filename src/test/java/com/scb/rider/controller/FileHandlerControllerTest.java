package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.RiderUploadedDocumentResponse;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FileHandlerControllerTest {

  private static final String PROFILE_ID =  "12345";
  private static final String FILE_NAME = "hello.png";

  @InjectMocks
  private FileHandlerController fileHandlerController;

  @Mock
  private RiderUploadedDocumentService service;


  @Test
  public void testFileUploadTest() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();

    when(service.uploadedDocument(any(MultipartFile.class), eq(PROFILE_ID),
        eq(DocumentType.PROFILE_PHOTO), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(Constants.OPS_MEMBER,
            PROFILE_ID, mockMultipartFile, DocumentType.PROFILE_PHOTO, FoodBoxSize.SMALL.name());
    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

  }


  @Test
  public void testFileUploadApplicationForm() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.BACKGROUND_VERIFICATION_FORM).build();

    when(service.uploadedDocument(any(MultipartFile.class), eq(PROFILE_ID),
        eq(DocumentType.BACKGROUND_VERIFICATION_FORM), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(Constants.OPS_MEMBER,
            PROFILE_ID, mockMultipartFile, DocumentType.BACKGROUND_VERIFICATION_FORM, FoodBoxSize.SMALL.name());
    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

  }

  @Test
  public void testFileUploadRiderVehiclewithFoodCart() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_WITH_FOOD_CARD).build();

    when(service.uploadedDocument(any(MultipartFile.class), eq(PROFILE_ID),
        eq(DocumentType.VEHICLE_WITH_FOOD_CARD), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(Constants.OPS_MEMBER,
            PROFILE_ID, mockMultipartFile, DocumentType.VEHICLE_WITH_FOOD_CARD, FoodBoxSize.SMALL.name());
    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

  }

  @Test(expected = InvalidImageExtensionException.class)
  public void testFileUploadTestThrowException() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();

    when(service.uploadedDocument(any(MultipartFile.class), eq(PROFILE_ID),
        eq(DocumentType.PROFILE_PHOTO), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenThrow(InvalidImageExtensionException.class);

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(Constants.OPS_MEMBER,
            PROFILE_ID, mockMultipartFile, DocumentType.PROFILE_PHOTO, FoodBoxSize.SMALL.name());
    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));

  }

  @Test
  public void testFileUploadTestFetch() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();

    when(service.fetchDocument(eq(PROFILE_ID), eq(DocumentType.PROFILE_PHOTO)))
        .thenReturn(java.util.Optional.of(uploadedDocument));

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity =
        (ResponseEntity<RiderUploadedDocumentResponse>) fileHandlerController
            .getUploadedFile(PROFILE_ID, DocumentType.PROFILE_PHOTO);
    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertNotNull(responseEntity.getBody().getDocumentType());
    assertNotNull(responseEntity.getBody().getRiderProfileId());
    assertNotNull(responseEntity.getBody().getId());
    assertNotNull(responseEntity.getBody().getImageUrl());
    assertNotNull(responseEntity.getBody().toString());


  }

  @Test
  public void testFileUploadTestFetchNotFound() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();

    when(service.fetchDocument(eq(PROFILE_ID), eq(DocumentType.PROFILE_PHOTO)))
        .thenReturn(java.util.Optional.empty());

    ResponseEntity<RiderUploadedDocumentResponse> responseEntity =
        (ResponseEntity<RiderUploadedDocumentResponse>) fileHandlerController
            .getUploadedFile(PROFILE_ID, DocumentType.PROFILE_PHOTO);
    assertFalse(ObjectUtils.isNotEmpty(responseEntity.getBody()));
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

  }


  @Test
  public void testDownloadFileTest() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    RiderUploadedDocument uploadedDocument =
        RiderUploadedDocument.builder().id("primaryKey").riderProfileId(PROFILE_ID).imageUrl("url")
            .documentType(DocumentType.VEHICLE_REGISTRATION).build();

    when(service.downloadDocument("5ff47aa5030d9e56f0997243", "1609864079957-sample.jpg"))
        .thenReturn(mockMultipartFile.getBytes());

    ResponseEntity<byte[]> responseEntity = fileHandlerController
        .getDownloadFile("5ff47aa5030d9e56f0997243", "1609864079957-sample.jpg", null);


    assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


  }

  @Test
  public void uploadMultipleDocumentsTest() throws InvalidImageExtensionException, IOException {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(service.uploadMultipleDocument(eq(PROFILE_ID), eq(DocumentType.BACKGROUND_VERIFICATION_FORM), any(MultipartFile[].class),
            eq(Constants.OPS_MEMBER), eq(Boolean.TRUE))).thenReturn(getRiderUploadedDocument());
    fileHandlerController.uploadMultipleDocument(Constants.OPS_MEMBER, PROFILE_ID,
            DocumentType.BACKGROUND_VERIFICATION_FORM, Boolean.TRUE, new MultipartFile[]{mockMultipartFile});
  }

  private RiderUploadedDocument getRiderUploadedDocument() {
    return RiderUploadedDocument.builder().riderProfileId(PROFILE_ID)
            .documentUrls(Arrays.asList(FILE_NAME)).build();
  }

}
