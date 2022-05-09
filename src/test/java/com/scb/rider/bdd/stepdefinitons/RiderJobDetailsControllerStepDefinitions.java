package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.kafka.KafkaPublisher;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles(value="test")
public class RiderJobDetailsControllerStepDefinitions {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    static final String URL = "/profile";
    private String POST_URL = "";
    private MvcResult result;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    private String profileId;
    private String id;
    @Mock
    private KafkaPublisher kafkaPublisher;
    @Mock
    private KafkaTemplate<String, RiderJobStatusEventModel> kafkaTemplate;
    @Mock
    private AmazonS3ImageService amazonS3ImageService;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        Optional<RiderProfile> riderProfile = riderProfileRepository.findByPhoneNumber("9718456515");
        if (!riderProfile.isPresent()) {
            AddressDto addressDto = AddressDto.builder()
                    .city("Bangkok")
                    .country("Thailand")
                    .countryCode("TH")
                    .district("district")
                    .floorNumber("1234")
                    .landmark("landmark")
                    .state("state")
                    .unitNumber("unitNumber")
                    .village("village")
                    .zipCode("203205").build();

            RiderProfileDto riderProfileDto = RiderProfileDto.builder()
                    .accountNumber("9718456515")
                    .address(addressDto)
                    .consentAcceptFlag(true)
                    .dataSharedFlag(true)
                    .firstName("Rohit").lastName("Sharma")
                    .nationalID("9718456515")
                    .dob("20/12/1988")
                    .phoneNumber("9718456515")
                    .build();
            RiderProfile riderProfileSave = new RiderProfile();
            BeanUtils.copyProperties(riderProfileDto, riderProfileSave);
            riderProfileSave = riderProfileRepository.save(riderProfileSave);
            profileId = riderProfileSave.getId();

        }else {
            profileId = riderProfile.get().getId();
        }
    }

    @Given("Set End Point to create Rider Job Details For JOB_ACCEPTED")
    public void set_end_point_to_create_rider_job_details_for_job_accepted() {
        POST_URL = URL + "/"+ profileId + "/job";
    }

    @When("Send a POST HTTP request with required information of JOB_ACCEPTED")
    public void send_a_post_http_request_with_required_information_of_job_accepted() throws Exception {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(POST_URL)
                //.file(mockMultipartFile)
                .param("jobStatus", "JOB_ACCEPTED")
                .param("remark", "string")
                //.param("parkingFee", "12.8787")
                .param("jobId", "9876543210")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

    }

    @Then("I receive valid JOB_ACCEPTED HTTP Response Code {int} and Rider Job Details")
    public void i_receive_valid_job_accepted_http_response_code_and_rider_job_details(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

        RiderJobDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderJobDetails.class);
        assertNotNull(riderJobDetails);
        assertNotNull(riderJobDetails.getId());
        assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
    }

}
