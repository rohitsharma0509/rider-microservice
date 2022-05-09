package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

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
import static org.mockito.ArgumentMatchers.anyString;

import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.service.document.RiderCovidSelfieService;
import com.scb.rider.util.CustomMultipartFile;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderCovidSelfieControllerVersion2Test {

	@InjectMocks
	private RiderCovidSelfieControllerVersion2 riderCovidSelfieController;

	@Mock
	private RiderCovidSelfieService service;

	@Test
	public void testCovidSelfieFileUploadTest() throws InvalidImageExtensionException, FileConversionException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());

		LocalDateTime localDateTime = LocalDateTime.now();

		RiderCovidSelfie riderCovidSelfie = RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
				.mimeType(mockMultipartFile.getContentType()).uploadedTime(localDateTime).build();

		when(service.uploadCovidSelfie(any(CustomMultipartFile.class), anyString(), any()))
				.thenReturn(riderCovidSelfie);
		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();

		ResponseEntity<RiderCovidSelfie> responseEntity = (ResponseEntity<RiderCovidSelfie>) riderCovidSelfieController
				.uploadCovidSelfieFile("12345", imageDto, localDateTime);
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}

	@Test(expected = InvalidImageExtensionException.class)
	public void testCovidSelfieFileUploadInvalidImageExtensionTest()
			throws InvalidImageExtensionException, FileConversionException {

		LocalDateTime localDateTime = LocalDateTime.now();

		when(service.uploadCovidSelfie(any(CustomMultipartFile.class), anyString(), any()))
				.thenThrow(InvalidImageExtensionException.class);
		ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();

		ResponseEntity<RiderCovidSelfie> responseEntity = (ResponseEntity<RiderCovidSelfie>) riderCovidSelfieController
				.uploadCovidSelfieFile("12345", imageDto, localDateTime);
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

	}

}
