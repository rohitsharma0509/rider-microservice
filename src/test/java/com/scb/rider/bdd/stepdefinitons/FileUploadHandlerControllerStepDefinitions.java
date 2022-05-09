/*
package com.scb.rider.bdd.stepdefinitons;


import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class FileUploadHandlerControllerStepDefinitions {

    private  String FILE_UPLOAD_URL = "";

    private static final String BUCKET_NAME = "bucket_name";
    private static final String KEY_NAME = "picture.jpg";

    MvcResult result;
    @Autowired
    protected MockMvc mockMvc;
    private AmazonS3 amazonS3;
    private TransferManager tm;
    private ProgressListener progressListener;
    private AmazonS3ImageService amazonS3ImageService;
    private  RiderUploadedDocumentService service;
    @Before
    public void setup() {
        amazonS3 = mock(AmazonS3.class);
        amazonS3ImageService = mock(AmazonS3ImageService.class);
        service = mock(RiderUploadedDocumentService.class);
        tm = TransferManagerBuilder
                .standard()
                .withS3Client(amazonS3)
                .withMultipartUploadThreshold((long) (5 * 1024 * 1025))
                .withExecutorFactory(() -> Executors.newFixedThreadPool(5))
                .build();
        progressListener =
                progressEvent -> System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred());
    }

    @Given("Set profile photo and docType for POST api endpoint")
    public void set_profile_photo_and_doc_type_for_post_api_endpoint() {
        FILE_UPLOAD_URL = "/profile/123/upload?docType="+ DocumentType.PROFILE_PHOTO;
    }

    @When("Send a POST HTTP request profile photo")
    public void send_a_post_http_request_profile_photo() throws Exception, InvalidImageExtensionException, FileConversionException {
        File file = mock(File.class);
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        PutObjectResult s3Result = mock(PutObjectResult.class);

        when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class))).thenReturn("imageUrl");
        // execute
         result = mockMvc.perform(MockMvcRequestBuilders
                .multipart(FILE_UPLOAD_URL)
                .file(mockMultipartFile))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

    }

    @Then("I receive valid profile photo POST HTTP Response Code {int}")
    public void i_receive_valid_profile_photo_post_http_response_code(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");

    }

    @Then("Send a GET HTTP request for Profile Photo")
    public void send_a_get_http_request_for_profile_photo() throws Exception {
        result = mockMvc
                .perform(
                        get(FILE_UPLOAD_URL))
                .andDo(print()).andReturn();

    }

    @Then("I receive valid profile photo GET HTTP Response code {int}")
    public void i_receive_valid_profile_photo_get_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

    }

    @Given("Set Vehicle Details and docType for POST api endpoint")
    public void set_vehicle_details_and_doc_type_for_post_api_endpoint() {
        FILE_UPLOAD_URL = "/profile/123/upload?docType="+ DocumentType.VEHICLE_REGISTRATION;

    }

    @When("Send a  Vehicle Details Photo POST HTTP request")
    public void send_a_vehicle_details_photo_post_http_request() throws Exception, InvalidImageExtensionException, FileConversionException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder()
                .id("primaryKey")
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.VEHICLE_REGISTRATION)
                .build();

        when(service.uploadedDocument(any(MultipartFile.class), eq("12345"),eq(DocumentType.VEHICLE_REGISTRATION)))
                .thenReturn(uploadedDocument);
        when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class))).thenReturn("imageUrl");

        result = mockMvc.perform(MockMvcRequestBuilders
                .multipart(FILE_UPLOAD_URL)
                .file(mockMultipartFile))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid  Vehicle Details Photo POST HTTP Response Code {int}")
    public void i_receive_valid_vehicle_details_photo_post_http_response_code(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");
    }

    @Then("Send a GET HTTP request for Vehicle Details Photo")
    public void send_a_get_http_request_for_vehicle_details_photo() throws Exception {
        result = mockMvc
                .perform(
                        get(FILE_UPLOAD_URL))
                .andDo(print()).andReturn();

    }

    @Then("I receive valid  Vehicle Details Photo GET HTTP Response code {int}")
    public void i_receive_valid_vehicle_details_photo_get_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

    }

    @Given("Set Driving License Details Photo and docType for POST api endpoint")
    public void set_driving_license_details_photo_and_doc_type_for_post_api_endpoint() {
        FILE_UPLOAD_URL = "/profile/123/upload?docType="+ DocumentType.DRIVER_LICENSE;

    }

    @When("Send a  Driving License Details Photo POST HTTP request")
    public void send_a_driving_license_details_photo_post_http_request() throws Exception, InvalidImageExtensionException, FileConversionException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder()
                .id("primaryKey")
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.DRIVER_LICENSE)
                .build();

        when(service.uploadedDocument(any(MultipartFile.class), eq("12345"),eq(DocumentType.DRIVER_LICENSE)))
                .thenReturn(uploadedDocument);
        when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class))).thenReturn("imageUrl");

        result = mockMvc.perform(MockMvcRequestBuilders
                .multipart(FILE_UPLOAD_URL)
                .file(mockMultipartFile))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid  Driving License Details Photo POST HTTP Response Code {int}")
    public void i_receive_valid_driving_license_details_photo_post_http_response_code(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");
    }

    @Then("Send a GET HTTP request for Driving License Details Photo")
    public void send_a_get_http_request_for_driving_license_details_photo() throws Exception {
        result = mockMvc
                .perform(
                        get(FILE_UPLOAD_URL))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid  Driving License Details Photo GET HTTP Response code {int}")
    public void i_receive_valid_driving_license_details_photo_get_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

    }

    @Given("Set Background Verification Form Photo and docType for POST api endpoint")
    public void set_background_verification_form_photo_and_doc_type_for_post_api_endpoint() {
        FILE_UPLOAD_URL = "/profile/123/upload?docType="+ DocumentType.BACKGROUND_VERIFICATION_FORM;

    }

    @When("Send a Background Verification Form POST HTTP request")
    public void send_a_background_verification_form_post_http_request() throws Exception, InvalidImageExtensionException, FileConversionException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder()
                .id("primaryKey")
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.BACKGROUND_VERIFICATION_FORM)
                .build();

        when(service.uploadedDocument(any(MultipartFile.class), eq("12345"),eq(DocumentType.BACKGROUND_VERIFICATION_FORM)))
                .thenReturn(uploadedDocument);
        when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class))).thenReturn("imageUrl");

        result = mockMvc.perform(MockMvcRequestBuilders
                .multipart(FILE_UPLOAD_URL)
                .file(mockMultipartFile))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid Background Verification Form POST HTTP Response Code {int}")
    public void i_receive_valid_background_verification_form_post_http_response_code(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");
    }

    @Then("Send a GET HTTP request for Background Verification Form Photo")
    public void send_a_get_http_request_for_background_verification_form_photo() throws Exception {
        result = mockMvc
                .perform(
                        get(FILE_UPLOAD_URL))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid Background Verification Form GET HTTP Response code {int}")
    public void i_receive_valid_background_verification_form_get_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

    }

    @Given("Set Vehicle with Food Cart and docType for POST api endpoint")
    public void set_vehicle_with_food_cart_and_doc_type_for_post_api_endpoint() {
        FILE_UPLOAD_URL = "/profile/123/upload?docType="+ DocumentType.VEHICLE_WITH_FOOD_CARD;

    }

    @When("Send a Vehicle with Food Cart Photo Form POST HTTP request")
    public void send_a_vehicle_with_food_cart_photo_form_post_http_request() throws Exception, InvalidImageExtensionException, FileConversionException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderUploadedDocument uploadedDocument = RiderUploadedDocument.builder()
                .id("primaryKey")
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.VEHICLE_WITH_FOOD_CARD)
                .build();

        when(service.uploadedDocument(any(MultipartFile.class), eq("12345"),eq(DocumentType.VEHICLE_WITH_FOOD_CARD)))
                .thenReturn(uploadedDocument);
        when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class))).thenReturn("imageUrl");

        result = mockMvc.perform(MockMvcRequestBuilders
                .multipart(FILE_UPLOAD_URL)
                .file(mockMultipartFile))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid Vehicle with Food Cart Photo POST HTTP Response Code {int}")
    public void i_receive_valid_vehicle_with_food_cart_photo_post_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");
    }

    @Then("Send a GET HTTP request for Vehicle with Food Cart")
    public void send_a_get_http_request_for_vehicle_with_food_cart() throws Exception {
        result = mockMvc
                .perform(
                        get(FILE_UPLOAD_URL))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid Vehicle with Food Cart Photo GET HTTP Response code {int}")
    public void i_receive_valid_vehicle_with_food_cart_photo_get_http_response_code(Integer int1) {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

    }

}
*/
