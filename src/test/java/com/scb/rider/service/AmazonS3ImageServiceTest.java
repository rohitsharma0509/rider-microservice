package com.scb.rider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.amazonaws.services.s3.AmazonS3;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@RunWith(MockitoJUnitRunner.class)
@PropertySource(value = "classpath:application.yml")
public class AmazonS3ImageServiceTest {

  private static final String PROFILE_ID =  "12345";
  private static final String FILE_NAME = "hello.png";

  @InjectMocks
  private AmazonS3ImageService amazonS3ImageService;

  @Mock
  private AmazonS3 s3Client;

  @Mock
  private AmazonClientService amazonClientService;

  @Test
  public void uploadFileTest() throws InvalidImageExtensionException, FileConversionException {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    String imageUrl = amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "12321", DocumentType.MEAL_PICKUP_PHOTO);

    assertNotNull(imageUrl, "Invalid Response");

  }

  @Test(expected = InvalidImageExtensionException.class)
  public void uploadFileTestInvalidExtension()
      throws InvalidImageExtensionException, FileConversionException {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    String imageurl = amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "12321", DocumentType.MEAL_PICKUP_PHOTO);
    System.out.println(imageurl);
    assertNotNull(imageurl, "Invalid Response");

  }

  @Test
  public void uploadFileTestRemoveFileFromAmzon() {
    String url =
        "https://s3.us-east-2.amazonaws.com/rider-document-stores/1607326396933-Blue_morpho_butterfly.jpg";
    assertTimeout(Duration.ofMillis(5000),
        () -> amazonS3ImageService.removeImageFromAmazon("Blue_morpho_butterfly.jpg", "12321", DocumentType.MEAL_PICKUP_PHOTO));
  }

  @Test
  public void removeMultipleFilesTest() {
    when(amazonClientService.getClient()).thenReturn(s3Client);
    amazonS3ImageService.removeMultipleFiles(Arrays.asList(FILE_NAME), PROFILE_ID);
    verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
  }

  @Test
  public void uploadMultipleFiles() throws InvalidImageExtensionException, IOException {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", FILE_NAME,
            MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
    when(amazonClientService.getClient()).thenReturn(s3Client);
    List<String> result = amazonS3ImageService.uploadMultipleFiles(new MultipartFile[] {mockMultipartFile}, PROFILE_ID);
    verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    assertNotNull(result);
    assertEquals(1, result.size());
  }

}
