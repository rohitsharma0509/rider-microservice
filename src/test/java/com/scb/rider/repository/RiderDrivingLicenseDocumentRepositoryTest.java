package com.scb.rider.repository;

import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class RiderDrivingLicenseDocumentRepositoryTest {
    @Autowired
    private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;

    private String createdId;
    @BeforeAll
    public void setUp() throws Exception {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument1 = RiderDrivingLicenseDocument.builder()
                .drivingLicenseNumber("12345")
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense("Permanent")
                .riderProfileId("1").build();
        RiderDrivingLicenseDocument riderDrivingLicenseDocument2 = RiderDrivingLicenseDocument.builder()
                .drivingLicenseNumber("123456")
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense("Permanent")
                .riderProfileId("2").build();
        //save Driver License Details, verify has ID value after save
        assertNull(riderDrivingLicenseDocument1.getId());
        assertNull(riderDrivingLicenseDocument2.getId());//null before save
        this.riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument1);
        this.riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument2);
        assertNotNull(riderDrivingLicenseDocument1.getId());
        assertNotNull(riderDrivingLicenseDocument2.getId());
        createdId = riderDrivingLicenseDocument2.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.findByDrivingLicenseNumber("123456");
        assertNotNull(riderDrivingLicenseDocument);
        assertEquals("123456", riderDrivingLicenseDocument.getDrivingLicenseNumber());
        /*Get all Driving License, list should only have two*/
        Iterable<RiderDrivingLicenseDocument> riderDrivingLicenseDocuments = riderDrivingLicenseDocumentRepository.findAll();
        int count = 0;
        for (RiderDrivingLicenseDocument riderDrivingLicense : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        Optional<RiderDrivingLicenseDocument> riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository
                .findById(createdId);
        assertNotNull(riderDrivingLicenseDocument);
        assertEquals("123456", riderDrivingLicenseDocument.get().getDrivingLicenseNumber());
        /*Get all Driving License, list should only have two*/
        Iterable<RiderDrivingLicenseDocument> riderDrivingLicenseDocuments = riderDrivingLicenseDocumentRepository.findAll();
        int count = 0;
        for (RiderDrivingLicenseDocument riderDrivingLicense : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testDataUpdate() {
        /*Test update*/
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.findByDrivingLicenseNumber("123456");
        riderDrivingLicenseDocument.setRiderProfileId("3");
        riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        RiderDrivingLicenseDocument updatedEntity = riderDrivingLicenseDocumentRepository.findByDrivingLicenseNumber("123456");
        assertNotNull(updatedEntity);
        assertEquals("3", updatedEntity.getRiderProfileId());
    }

    @AfterAll
    public void tearDown() throws Exception {
        this.riderDrivingLicenseDocumentRepository.deleteAll();
    }

}
