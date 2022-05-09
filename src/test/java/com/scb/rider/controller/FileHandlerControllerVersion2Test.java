package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.scb.rider.model.enumeration.FoodBoxSize;
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

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.model.dto.RiderUploadedDocumentResponse;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import com.scb.rider.util.CustomMultipartFile;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FileHandlerControllerVersion2Test {

	private static final String PROFILE_ID = "12345";
	private static final String FILE_NAME = "hello.png";

	@InjectMocks
	private FileHandlerControllerVersion2 fileHandlerController;

	@Mock
	private RiderUploadedDocumentService service;

	@Test
	public void testFileUploadTest() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME, MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());

		RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey")
				.riderProfileId(PROFILE_ID).imageUrl("url").documentType(DocumentType.VEHICLE_REGISTRATION).build();

		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();
		when(service.uploadedDocument(any(CustomMultipartFile.class), eq(PROFILE_ID), eq(DocumentType.PROFILE_PHOTO),
				eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

		ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(PROFILE_ID,
				imageDto, Constants.OPS_MEMBER, DocumentType.PROFILE_PHOTO, FoodBoxSize.SMALL.name());
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}

	@Test
	public void testMultipleFileUploadTest() throws Exception {

		
		RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey")
				.riderProfileId(PROFILE_ID).imageUrl("url").documentType(DocumentType.VEHICLE_REGISTRATION).build();

		ImageDto[] imageDto = new ImageDto[1];
		imageDto[0] = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();
		
		when(service.uploadMultipleDocument(eq(PROFILE_ID), eq(DocumentType.PROFILE_PHOTO),
				any(CustomMultipartFile[].class), anyString(), anyBoolean())).thenReturn(uploadedDocument);
		
		ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadMultipleDocument("123",PROFILE_ID , DocumentType.PROFILE_PHOTO, true, imageDto);
		
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}
	
	
	@Test
	public void testFileUploadApplicationForm() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME, MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey")
				.riderProfileId(PROFILE_ID).imageUrl("url").documentType(DocumentType.BACKGROUND_VERIFICATION_FORM)
				.build();
		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();

		when(service.uploadedDocument(any(CustomMultipartFile.class), eq(PROFILE_ID),
				eq(DocumentType.BACKGROUND_VERIFICATION_FORM), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

		ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(PROFILE_ID,
				imageDto, Constants.OPS_MEMBER, DocumentType.BACKGROUND_VERIFICATION_FORM, FoodBoxSize.SMALL.name());
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}

	@Test
	public void testFileUploadRiderVehiclewithFoodCart() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME, MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());

		RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey")
				.riderProfileId(PROFILE_ID).imageUrl("url").documentType(DocumentType.VEHICLE_WITH_FOOD_CARD).build();
		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();

		when(service.uploadedDocument(any(CustomMultipartFile.class), eq(PROFILE_ID),
				eq(DocumentType.VEHICLE_WITH_FOOD_CARD), eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenReturn(uploadedDocument);

		ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(PROFILE_ID,
				imageDto, Constants.OPS_MEMBER, DocumentType.VEHICLE_WITH_FOOD_CARD, FoodBoxSize.SMALL.name());
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}

	@Test(expected = InvalidImageExtensionException.class)
	public void testFileUploadTestThrowException() throws Exception {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());

		RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder().id("primaryKey")
				.riderProfileId(PROFILE_ID).imageUrl("url").documentType(DocumentType.VEHICLE_REGISTRATION).build();
		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();

		when(service.uploadedDocument(any(CustomMultipartFile.class), eq(PROFILE_ID), eq(DocumentType.PROFILE_PHOTO),
				eq(Constants.OPS_MEMBER), eq(FoodBoxSize.SMALL.name()))).thenThrow(InvalidImageExtensionException.class);

		ResponseEntity<RiderUploadedDocumentResponse> responseEntity = fileHandlerController.uploadFile(PROFILE_ID,
				imageDto, Constants.OPS_MEMBER, DocumentType.PROFILE_PHOTO, FoodBoxSize.SMALL.name());
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));

	}

}
