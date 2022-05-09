package com.scb.rider.IntegrationTest.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderBackgroundVerificationDocumentRepository;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public abstract class AbstractRestApiIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected RiderDrivingLicenseDocumentRepository drivingLicenseDocumentRepository;
    @Autowired
    protected RiderVehicleRegistrationRepository vehicleRegistrationRepository;
    @Autowired
    protected RiderProfileRepository riderProfileRepository;
    @Autowired
    protected RiderJobDetailsRepository riderJobDetailsRepository;
    @Autowired
    protected RiderDeviceDetailRepository riderDeviceDetailRepository;

    @Autowired
    protected RiderBackgroundVerificationDocumentRepository riderBackgroundRepository;

    @BeforeEach
    void initializeDatabase() {

    }

    @Transactional
    protected RiderDrivingLicenseDocument createRiderDrivingLicenseDocumentDb() {
        RiderDrivingLicenseDocument drivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("abcd-abcd")
                .drivingLicenseNumber("1234")
                .dateOfExpiry(LocalDate.of(2022, 12, 30))
                .dateOfIssue(LocalDate.of(2012, 12, 30))
                .typeOfLicense("Permanent")
                .riderProfileId("profile-id-1")
                .documentUrl("testurl")
                .build();
        return drivingLicenseDocumentRepository.save(drivingLicenseDocument);
    }

    @Transactional
    protected RiderProfile createRiderProfileDb() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setFirstName("Sachin");
        riderProfile.setNationalID("123456789000");
        riderProfile.setPhoneNumber("5555555566");
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        RiderPreferredZones riderPreferredZones =new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("2");
        riderPreferredZones.setPreferredZoneName("test");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        return riderProfileRepository.save(riderProfile);
    }

    @Transactional
    protected RiderJobDetails createRiderJobDetailInDb(String riderID) {
        RiderJobDetails riderJobDetail = new RiderJobDetails();
        riderJobDetail.setParkingPhotoUrl("Sachin.jpg");
        riderJobDetail.setMealPhotoUrl("mealphoto.jpg");
        riderJobDetail.setParkingFee(new BigDecimal("123"));
        riderJobDetail.setJobId("9876543210");
        riderJobDetail.setProfileId(riderID);
        riderJobDetail.setRemarks("Remark");
        riderJobDetail.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        return riderJobDetailsRepository.save(riderJobDetail);
    }

    @Transactional
    protected RiderJobDetails createRiderJobDetailInDb(String riderID, RiderJobStatus jobStatus) {
        RiderJobDetails riderJobDetail = new RiderJobDetails();
        riderJobDetail.setParkingPhotoUrl("Sachin.jpg");
        riderJobDetail.setMealPhotoUrl("mealphoto.jpg");
        riderJobDetail.setParkingFee(new BigDecimal("123"));
        riderJobDetail.setJobId("9876543210");
        riderJobDetail.setProfileId(riderID);
        riderJobDetail.setRemarks("Remark");
        riderJobDetail.setJobStatus(jobStatus);
        return riderJobDetailsRepository.save(riderJobDetail);
    }

    @Transactional
    protected RiderVehicleRegistrationDocument createRiderVehicleRegistrationDocumentDb() {
        RiderVehicleRegistrationDocument vehicleRegistrationDocument = RiderVehicleRegistrationDocument.builder()
                .id("abcd-abcd")
                .registrationNo("1234")
                .expiryDate(LocalDate.of(2022, 12, 30))
                .registrationDate(LocalDate.of(2012, 12, 30))
                .registrationCardId("12344")
                .province("abcd")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .riderProfileId("profile-id-1")
                .build();
        return vehicleRegistrationRepository.save(vehicleRegistrationDocument);
    }

    @Transactional
    protected RiderDeviceDetails createRiderDeviceInfoInDb() {
        RiderDeviceDetails riderDeviceDetails = RiderDeviceDetails.builder()
                .id("abcd-abcd")
                .profileId("1234")
                .deviceToken("device-token")
                .arn("arn")
                .platform(Platform.GCM)
                .build();
        return riderDeviceDetailRepository.save(riderDeviceDetails);
    }
    @Transactional
    protected RiderBackgroundVerificationDocument createRiderBackgroundDocumentDb() {
        RiderBackgroundVerificationDocument riderBackgroundDoc = RiderBackgroundVerificationDocument.builder()
                .id("1222").riderProfileId("111").reason("test")
                .dueDate(LocalDate.of(2012, 12, 30)).documentUrls(Arrays.asList("url"))
                .status(MandatoryCheckStatus.APPROVED)
                .build();
        return riderBackgroundRepository.save(riderBackgroundDoc);
    }

    @AfterEach
    void cleanUpDatabase() {
        drivingLicenseDocumentRepository.deleteAll();
        riderProfileRepository.deleteAll();
        riderJobDetailsRepository.deleteAll();
    }
}
