package com.scb.rider.repository;

import com.scb.rider.model.document.RiderCovidSelfie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class RiderCovidSelfieRepositoryTest {


    @Autowired
    private RiderCovidSelfieRepository riderCovidSelfieRepository;

    private String createdId;

    @BeforeAll
    public void setUp() throws Exception {
        RiderCovidSelfie riderCovidSelfie = RiderCovidSelfie.builder()
                .uploadedTime(LocalDateTime.now())
                .fileName("Temp.jpg")
                .riderId("1234")
                .build();

        RiderCovidSelfie riderCovidSelfie2 = RiderCovidSelfie.builder()
                .uploadedTime(LocalDateTime.now())
                .fileName("Temp.jpg")
                .riderId("12345")
                .build();

        //save Details, verify has ID value after save
        assertNull(riderCovidSelfie.getId());
        assertNull(riderCovidSelfie2.getId());//null before save
        this.riderCovidSelfieRepository.save(riderCovidSelfie);
        this.riderCovidSelfieRepository.save(riderCovidSelfie2);
        assertNotNull(riderCovidSelfie.getId());
        assertNotNull(riderCovidSelfie2.getId());

        createdId = riderCovidSelfie.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        Optional<RiderCovidSelfie> riderCovidSelfie = riderCovidSelfieRepository
                .findById(createdId);

        assertNotNull(riderCovidSelfie.get());
        assertEquals("1234", riderCovidSelfie.get().getRiderId());
        /*Get all Details, list should only have two*/
        List<RiderCovidSelfie> riderCovidSelfieList = riderCovidSelfieRepository.findAll();
        int count = 0;
        for (RiderCovidSelfie document : riderCovidSelfieList) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0,1);
        Page<RiderCovidSelfie> riderCovidSelfieList = riderCovidSelfieRepository
                .findByRiderIdAndUploadedTimeBetween("1234", from, to,pageable);
        assertNotNull(riderCovidSelfieList);
        assertEquals(1, riderCovidSelfieList.getContent().size());
        /*Get all , list should only have two*/
        List<RiderCovidSelfie> allRiderCovidSelfieList = riderCovidSelfieRepository.findAll();
        int count = 0;
        for (RiderCovidSelfie document : allRiderCovidSelfieList) {
            count++;
        }
        assertEquals(2, count);
    }


    @AfterAll
    public void tearDown() throws Exception {
        this.riderCovidSelfieRepository.deleteAll();
    }
}
