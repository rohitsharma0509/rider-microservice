package com.scb.rider.interservice.IntegrationTest.controller;

import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;
import com.scb.rider.interservice.controller.JobServiceProxyController;
import com.scb.rider.repository.RiderJobDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class JobServiceProxyControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private JobServiceProxyController jobServiceProxyController;

    @Mock
    private RiderJobDetailsRepository riderJobDetailsCustomRepository;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(jobServiceProxyController).build();
    }

    @Test
    public void getRunningJobsForRider_WhenCannotConnectMONGODB() {
        String riderId = "6194f9db6f902c43de51b15c";

        when(riderJobDetailsCustomRepository.findRunningJobIdForRider(riderId))
                .thenThrow(new MongoSocketOpenException("Exception opening socket", new ServerAddress()));

        assertThrows(Exception.class, () -> {
            this.mvc.perform(get("/profile/job/running/rider/{riderId}", riderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8"))
                    .andExpect(status().isInternalServerError())
                    .andReturn();
        });
    }

    @Test
    public void getRunningJobsForRider_WhenRiderIdIsSpace() throws Exception {
        String riderId = " ";
        assertThrows(Exception.class, () -> {
            this.mvc.perform(get("/profile/job/running/rider/{riderId}", riderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8"))
                    .andExpect(status().isInternalServerError())
                    .andReturn();
        });
    }

    @Test
    public void getRunningJobsForRider_WhenRiderIdIsEmpty() throws Exception {
        String riderId = "";
        this.mvc.perform(get("/profile/job/running/rider/{riderId}", riderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getRunningJobsForRider_WhenRiderIdIsNotSent() throws Exception {
        this.mvc.perform(get("/profile/job/running/rider/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getRunningJobsForRider_WhenRiderIdIsNull() throws Exception {
        String riderId = null;
        this.mvc.perform(get("/profile/job/running/rider/{riderId}", riderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getRunningJobsForRider_WhenRiderIdIsNoLongerExist() throws Exception {
        String riderId = "no_longer_exist";
        assertThrows(Exception.class, () -> {
            this.mvc.perform(get("/profile/job/running/rider/{riderId}", riderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8"))
                    .andExpect(status().isInternalServerError())
                    .andReturn();
        });
    }
}
