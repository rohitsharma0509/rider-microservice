package com.scb.rider.repository;

import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.enumeration.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class RiderDeviceDetailsRepositoryTest {

    @Autowired
    private RiderDeviceDetailRepository riderDeviceDetailRepository;

    private String createdId;

    @BeforeAll
    public void setUp() throws Exception {
        RiderDeviceDetails riderDeviceDetails = RiderDeviceDetails.builder()
               .deviceToken("DeviceToke")
                .arn("Device-Arn")
                .platform(Platform.GCM)
                .profileId("1234")
                .build();

        RiderDeviceDetails riderDeviceDetails2 = RiderDeviceDetails.builder()
                .deviceToken("DeviceToken-2")
                .arn("Device-Arn-2")
                .platform(Platform.APNS_SANDBOX)
                .profileId("12345")
                .build();

        //save Details, verify has ID value after save
        assertNull(riderDeviceDetails.getId());
        assertNull(riderDeviceDetails2.getId());//null before save
        this.riderDeviceDetailRepository.save(riderDeviceDetails);
        this.riderDeviceDetailRepository.save(riderDeviceDetails2);
        assertNotNull(riderDeviceDetails.getId());
        assertNotNull(riderDeviceDetails2.getId());

        createdId = riderDeviceDetails.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        Optional<RiderDeviceDetails> riderJob = riderDeviceDetailRepository
                .findByProfileId("12345");
        assertNotNull(riderJob.get());
        assertEquals("12345", riderJob.get().getProfileId());
        /*Get all Details, list should only have two*/
        List<RiderDeviceDetails> riderDeviceDetails = riderDeviceDetailRepository.findAll();
        int count = 0;
        for (RiderDeviceDetails document : riderDeviceDetails) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        Optional<RiderDeviceDetails> riderJob = riderDeviceDetailRepository
                .findById(createdId);
        assertNotNull(riderJob);
        assertEquals("1234", riderJob.get().getProfileId());
        /*Get all , list should only have two*/
        List<RiderDeviceDetails> riderDeviceDetails = riderDeviceDetailRepository.findAll();
        int count = 0;
        for (RiderDeviceDetails riderDevice : riderDeviceDetails) {
            count++;
        }
        assertEquals(2, count);
    }


    @AfterAll
    public void tearDown() throws Exception {
        this.riderDeviceDetailRepository.deleteAll();
    }
}
