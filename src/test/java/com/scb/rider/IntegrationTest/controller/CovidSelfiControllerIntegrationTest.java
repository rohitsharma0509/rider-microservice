package com.scb.rider.IntegrationTest.controller;


import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderCovidSelfieData;
import com.scb.rider.service.AmazonS3ImageService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CovidSelfiControllerIntegrationTest extends AbstractRestApiIntegrationTest{
    static final String URL = "/profile/";

    @MockBean
    private AmazonS3ImageService amazonS3ImageService;

    @Before
    public void setUp() throws IOException {
        doNothing().when(amazonS3ImageService.uploadInputStream(any(),any(),any(),any()));
    }


    @Test
    public void testUploadedCovidSelfieSuccess() throws Exception {

        // prepare data and mock's behaviour
        RiderProfile riderProfile = createRiderProfileDb();
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/covid-selfie")
                .file(mockMultipartFile)
                .param("uploadedTime", String.valueOf(LocalDateTime.now()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

        RiderCovidSelfie covidSelfie = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderCovidSelfie.class);
        assertNotNull(covidSelfie);
        assertNotNull(covidSelfie.getId());
        assertEquals("text/plain", covidSelfie.getMimeType(), "Invalid Mime Type");
    }

    @Test
    public void testUploadedCovidSelfieBadRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderProfile riderProfile = createRiderProfileDb();
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/covid-selfie")
                .file(mockMultipartFile)
                //.param("uploadedTime", String.valueOf(LocalDateTime.now()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");

    }

    @Test
    public void testFetchCovidSelfieSuccess() throws Exception {

        // prepare data and mock's behaviour
        RiderProfile riderProfile = createRiderProfileDb();
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        //
        MvcResult upresult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/covid-selfie")
                .file(mockMultipartFile)
                .param("uploadedTime", String.valueOf(LocalDateTime.now()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        // verify
        int cstatus = upresult.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), cstatus, "Incorrect Response Status");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + riderProfile.getId() + "/covid-selfie/download")
                .param("from", String.valueOf(LocalDateTime.now().toLocalDate().atTime(LocalTime.MIN)))
                .param("to", String.valueOf(LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderCovidSelfieData covidSelfie = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderCovidSelfieData.class);
        assertNotNull(covidSelfie);
        assertNotNull(covidSelfie.getTotalCount());
        assertEquals(1, covidSelfie.getTotalPages(), "Invalid Page Size");
    }
    @Test
    public void testFetchCovidSelfieBadRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderProfile riderProfile = createRiderProfileDb();
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        //
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + riderProfile.getId() + "/covid-selfie/download")
                .param("to", String.valueOf(LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");

    }

}
