package com.scb.rider.repository;

import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class RiderVehicleRegistrationRepositoryTest {

    @Autowired
    private RiderVehicleRegistrationRepository riderVehicleRegistrationRepository;

    private String createdId;
    @BeforeAll
    public void setUp() throws Exception {
        RiderVehicleRegistrationDocument riderVehicleRegistrationDocument1 = RiderVehicleRegistrationDocument.builder()
                .registrationNo("12345")
                .registrationCardId("12345")
                .registrationDate(LocalDate.now())
                .expiryDate(LocalDate.now())
                .makerModel("BMW")
                .province("ABCD")
                .riderProfileId("1").build();
        RiderVehicleRegistrationDocument riderVehicleRegistrationDocument2 = RiderVehicleRegistrationDocument.builder()
                .registrationNo("123456")
                .registrationCardId("123456")
                .registrationDate(LocalDate.now())
                .expiryDate(LocalDate.now())
                .makerModel("BMW")
                .province("ABCD")
                .riderProfileId("2").build();
        //save Driver License Details, verify has ID value after save
        assertNull(riderVehicleRegistrationDocument1.getId());
        assertNull(riderVehicleRegistrationDocument2.getId());//null before save
        this.riderVehicleRegistrationRepository.save(riderVehicleRegistrationDocument1);
        this.riderVehicleRegistrationRepository.save(riderVehicleRegistrationDocument2);
        assertNotNull(riderVehicleRegistrationDocument1.getId());
        assertNotNull(riderVehicleRegistrationDocument2.getId());
        assertNotNull(riderVehicleRegistrationDocument2.getCreatedDate());
        assertNotNull(riderVehicleRegistrationDocument2.getUpdatedDate());
        createdId = riderVehicleRegistrationDocument2.getId();

    }

    @Test
    public void testFetchData() {
        /*Test data retrieval*/
        Optional<RiderVehicleRegistrationDocument> riderVehicleRegistrationDocument = riderVehicleRegistrationRepository.findByRiderProfileId("2");
        assertNotNull(riderVehicleRegistrationDocument.get());
        assertEquals("123456", riderVehicleRegistrationDocument.get().getRegistrationNo());
        /*Get all Details, list should only have two*/
        List<RiderVehicleRegistrationDocument> riderDrivingLicenseDocuments = riderVehicleRegistrationRepository.findAll();
        int count = 0;
        for (RiderVehicleRegistrationDocument riderVehicleReg : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testFetchDataById() {
        /*Test data retrieval*/
        Optional<RiderVehicleRegistrationDocument> vehicleReg = riderVehicleRegistrationRepository
                .findById(createdId);
        assertNotNull(vehicleReg);
        assertEquals("123456", vehicleReg.get().getRegistrationNo());
        /*Get all , list should only have two*/
        List<RiderVehicleRegistrationDocument> riderDrivingLicenseDocuments = riderVehicleRegistrationRepository.findAll();
        int count = 0;
        for (RiderVehicleRegistrationDocument vehicleRegistrationDocument : riderDrivingLicenseDocuments) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testDataUpdate() {
        /*Test update*/
        RiderVehicleRegistrationDocument vehicleRegistrationDocument = riderVehicleRegistrationRepository.findByRiderProfileId("2").get();
        vehicleRegistrationDocument.setRiderProfileId("3");
        riderVehicleRegistrationRepository.save(vehicleRegistrationDocument);
        RiderVehicleRegistrationDocument updatedEntity = riderVehicleRegistrationRepository.findByRiderProfileId("3").get();
        assertNotNull(updatedEntity);
        assertEquals("3", updatedEntity.getRiderProfileId());
    }

    @AfterAll
    public void tearDown() throws Exception {
        this.riderVehicleRegistrationRepository.deleteAll();
    }

}
