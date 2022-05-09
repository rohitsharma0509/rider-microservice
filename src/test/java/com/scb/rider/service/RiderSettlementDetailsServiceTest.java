package com.scb.rider.service;


import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.model.document.Address;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.JobSettlementDetails;
import com.scb.rider.model.dto.RiderSettlementDetails;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderSettlementDetailsServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;
    @Mock
    private RiderProfileRepository riderProfileRepository;
    @Mock
    private JobServiceFeignClient jobServiceFeignClient;
    @InjectMocks
    private RiderSettlementDetailsService riderSettlementDetailsService;
    RiderJobDetails riderJobDetails;
    JobSettlementDetails jobSettlementDetails;
    RiderProfile riderProfile;
    @Test
    public void getDataForReconciliationTest() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();
        riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        List<RiderJobDetails> riderJobDetailsList = new ArrayList<>();
        riderJobDetailsList.add(riderJobDetails);

        when(riderJobDetailsRepository.findRiderJobReconciliationDetails(any(), any()))
                .thenReturn(riderJobDetailsList);
        jobSettlementDetails = new JobSettlementDetails();
        jobSettlementDetails.setJobId("1234");
        jobSettlementDetails.setNetPrice(new BigDecimal("123.90"));
        jobSettlementDetails.setNormalPrice(new BigDecimal("130.90"));
        jobSettlementDetails.setOrderId("98219");

        List<JobSettlementDetails> jobSettlementDetailsList = new ArrayList<>();
        jobSettlementDetailsList.add(jobSettlementDetails);

        when(jobServiceFeignClient.getJobDetails(anyList())).thenReturn(jobSettlementDetailsList);

        Address address = new Address();
        address.setCity("Bangkok");
        address.setCountry("Thailand");
        address.setCountryCode("TH");
        address.setDistrict("district");
        address.setFloorNumber("1234");
        address.setLandmark("landmark");
        address.setState("state");
        address.setUnitNumber("unitNumber");
        address.setVillage("village");
        address.setZipCode("203205");

        riderProfile = new RiderProfile();
        riderProfile.setId("123");
        riderProfile.setRiderId("RR0001");
        riderProfile.setAccountNumber("1212121212");
        riderProfile.setAddress(address);
        riderProfile.setConsentAcceptFlag(true);
        riderProfile.setDataSharedFlag(true);
        riderProfile.setFirstName("Rohit");
        riderProfile.setLastName("Sharma");
        riderProfile.setNationalID("1234567890");
        riderProfile.setDob("20/12/1988");
        riderProfile.setPhoneNumber("9999999999");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.PENDING);
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);

        List<RiderProfile> riderProfiles = new ArrayList<>();
        riderProfiles.add(riderProfile);

        when(riderProfileRepository.findByIdIn(anyList())).thenReturn(riderProfiles);

        List<RiderSettlementDetails> jobList =
                riderSettlementDetailsService.getRiderSettlementDetails(startTime,endTime);

        assertTrue(ObjectUtils.isNotEmpty(jobList));
        assertNotNull(jobSettlementDetails.toString());
    }


}
