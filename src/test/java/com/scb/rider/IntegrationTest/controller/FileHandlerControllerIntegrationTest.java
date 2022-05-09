package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderUploadedDocumentResponse;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileHandlerControllerIntegrationTest {

  static String FILE_UPLOAD;

  static String FILE_DOWNLOAD;

  @MockBean
  private AmazonS3ImageService amazonS3ImageService;
  private RiderProfile riderProfile;
  private static final String ACCOUNT_NO = "121212121212121";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RiderProfileRepository riderProfileRepository;

  @Test
  @Order(1)
  void deleteData() {
    Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber(ACCOUNT_NO);
    if(rider.isPresent()) {
      riderProfileRepository.delete(rider.get());
    }
  }

  @Test
  @Order(2)
  void createRiderDetails() {
    riderProfile = new RiderProfile();
    riderProfile.setAccountNumber(ACCOUNT_NO);
    riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));
    riderProfile = riderProfileRepository.save(riderProfile);
    FILE_UPLOAD = String.format("/profile/%s/upload", riderProfile.getId());
    FILE_DOWNLOAD = String.format("/profile/%s/downlaod", riderProfile.getId());
  }

  @Test
  void testDocumentUploadWithValidExtension() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("imageUrl");
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .multipart(FILE_UPLOAD + "?docType=" + DocumentType.PROFILE_PHOTO).file(mockMultipartFile))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

    RiderUploadedDocumentResponse uploadedDocumentResponse = objectMapper
        .readValue(result.getResponse().getContentAsString(), RiderUploadedDocumentResponse.class);
    assertNotNull(uploadedDocumentResponse);
    assertNotNull(uploadedDocumentResponse.getId());
    assertNotNull(uploadedDocumentResponse.getImageUrl());
    assertNotNull(uploadedDocumentResponse.toString());
  }

  @Test
  void testDocumentUploadWithValidExtensionBadRequest() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenReturn("imageUrl");
    // execute
    MvcResult result =
        mockMvc.perform(MockMvcRequestBuilders.multipart(FILE_UPLOAD).file(mockMultipartFile))
            .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");

  }

  @Test
  void testDocumentUploadWithInValidExtension() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenThrow(IllegalMonitorStateException.class);
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .multipart(FILE_UPLOAD + "?docType=" + DocumentType.PROFILE_PHOTO).file(mockMultipartFile))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status, "Incorrect Response Status");
  }

  @Test
  void testDocumentUploadWthFileConversionException() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any()))
        .thenThrow(FileConversionException.class);
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .multipart(FILE_UPLOAD + "?docType=" + DocumentType.PROFILE_PHOTO).file(mockMultipartFile))
        .andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status, "Incorrect Response Status");

  }


  @Test
  void testDocumentDownloadWithInValidExtension() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text",
        MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

    when(amazonS3ImageService.downloadFile("12345", "1609864079957-sample.jpg"))
        .thenReturn(mockMultipartFile.getBytes());
    // execute
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
        .multipart(FILE_DOWNLOAD + "?documentKey=" + "1609864079957-sample.jpg")
        .file(mockMultipartFile)).andDo(print()).andReturn();

    // verify
    int status = result.getResponse().getStatus();
    assertEquals(HttpStatus.NOT_FOUND.value(), status, "Correct Response Status");

  }

}
