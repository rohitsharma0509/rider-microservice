package com.scb.rider.repository;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderUploadedDocument;
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
public class RiderUploadedDocumentRepositoryTest {

    @Autowired
    private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

    private String createdId;
    @BeforeAll
    public void setUp() throws Exception {
        RiderUploadedDocument uploadedDocument1 = RiderUploadedDocument.builder()
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.DRIVER_LICENSE)
                .build();
        RiderUploadedDocument uploadedDocument2 = RiderUploadedDocument.builder()
                .riderProfileId("12345")
                .imageUrl("url")
                .documentType(DocumentType.VEHICLE_REGISTRATION)
                .build();
        //save Details, verify has ID value after save
        assertNull(uploadedDocument1.getId());
        assertNull(uploadedDocument2.getId());//null before save
        this.riderUploadedDocumentRepository.save(uploadedDocument1);
        this.riderUploadedDocumentRepository.save(uploadedDocument2);
        assertNotNull(uploadedDocument1.getId());
        assertNotNull(uploadedDocument2.getId());
        assertNotNull(uploadedDocument2.getCreatedDate());
        assertNotNull(uploadedDocument2.getUpdatedDate());
        createdId = uploadedDocument1.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        Optional<RiderUploadedDocument> riderUploadedDocument = riderUploadedDocumentRepository
                .findByRiderProfileIdAndDocumentType("12345",DocumentType.DRIVER_LICENSE);
        assertNotNull(riderUploadedDocument.get());
        assertEquals("12345", riderUploadedDocument.get().getRiderProfileId());
        /*Get all Details, list should only have two*/
        List<RiderUploadedDocument> riderDrivingLicenseDocuments = riderUploadedDocumentRepository.findAll();
        int count = 0;
        for (RiderUploadedDocument document : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        Optional<RiderUploadedDocument> vehicleReg = riderUploadedDocumentRepository
                .findById(createdId);
        assertNotNull(vehicleReg);
        assertEquals("12345", vehicleReg.get().getRiderProfileId());
        /*Get all , list should only have two*/
        List<RiderUploadedDocument> riderDrivingLicenseDocuments = riderUploadedDocumentRepository.findAll();
        int count = 0;
        for (RiderUploadedDocument vehicleRegistrationDocument : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testDataUpdate() {
        /*Test update*/
        RiderUploadedDocument document = riderUploadedDocumentRepository
                .findByRiderProfileIdAndDocumentType("12345",DocumentType.DRIVER_LICENSE).get();
        document.setRiderProfileId("121212");
        riderUploadedDocumentRepository.save(document);
        RiderUploadedDocument updatedEntity = riderUploadedDocumentRepository
                .findByRiderProfileIdAndDocumentType("121212",DocumentType.DRIVER_LICENSE).get();
        assertNotNull(updatedEntity);
        assertEquals("121212", updatedEntity.getRiderProfileId());
    }

    @AfterAll
    public void tearDown() throws Exception {
        this.riderUploadedDocumentRepository.deleteAll();
    }

}
