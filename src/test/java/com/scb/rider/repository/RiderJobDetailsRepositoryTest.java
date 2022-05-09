package com.scb.rider.repository;

import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.enumeration.RiderJobStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class RiderJobDetailsRepositoryTest {

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    private String createdId;

    @BeforeAll
    public void setUp() throws Exception {
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        RiderJobDetails riderJobDetail = new RiderJobDetails();
        riderJobDetail.setJobId("12345");
        riderJobDetail.setProfileId("123");
        riderJobDetail.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        riderJobDetail.setRemarks("remark");
        riderJobDetail.setParkingFee(new BigDecimal("120"));
        riderJobDetail.setMealPhotoUrl("meal_photo1.jpg");
        riderJobDetail.setParkingPhotoUrl("parking_photo1.jpg");
        riderJobDetail.setCreatedDate(LocalDateTime.now());
        riderJobDetail.setUpdatedDate(LocalDateTime.now());


        //save Details, verify has ID value after save
        assertNull(riderJobDetails.getId());
        assertNull(riderJobDetail.getId());//null before save
        this.riderJobDetailsRepository.save(riderJobDetail);
        this.riderJobDetailsRepository.save(riderJobDetails);
        assertNotNull(riderJobDetail.getId());
        assertNotNull(riderJobDetails.getId());
        assertNotNull(riderJobDetail.getCreatedDate());
        assertNotNull(riderJobDetails.getUpdatedDate());
        assertNotNull(riderJobDetail.toString());
        assertNotNull(riderJobDetail.hashCode());
        assertTrue(riderJobDetail.equals(riderJobDetail));
        createdId = riderJobDetail.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        Optional<RiderJobDetails> riderJob = riderJobDetailsRepository
                .findByJobId("12345");
        assertNotNull(riderJob.get());
        assertEquals("12345", riderJob.get().getJobId());
        /*Get all Details, list should only have two*/
        List<RiderJobDetails> riderJobDetailsList = riderJobDetailsRepository.findAll();
        int count = 0;
        for (RiderJobDetails document : riderJobDetailsList) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        Optional<RiderJobDetails> riderJob = riderJobDetailsRepository
                .findById(createdId);
        assertNotNull(riderJob);
        assertEquals("12345", riderJob.get().getJobId());
        /*Get all , list should only have two*/
        List<RiderJobDetails> riderUploadedDocumentList = riderJobDetailsRepository.findAll();
        int count = 0;
        for (RiderJobDetails riderJobL : riderUploadedDocumentList) {
            count++;
        }
        assertEquals(2, count);
    }


    @AfterAll
    public void tearDown() throws Exception {
        this.riderJobDetailsRepository.deleteAll();
    }
}
