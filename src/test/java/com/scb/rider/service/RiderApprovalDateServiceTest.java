package com.scb.rider.service;

import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderApprovalDateServiceTest {

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @InjectMocks
    private RiderApprovalDateService riderApprovalDateService;

    @Test
    public void updateTest(){
        when(riderProfileRepository.findAllByStatusInAndApprovalDateTimeIsNull(any(),  any())).thenReturn(getRiders());
        when(riderProfileRepository.countByStatusInAndApprovalDateTimeIsNull(any())).thenReturn(0L);
        when(riderJobDetailsRepository.findTop1ByProfileIdOrderByJobAcceptedTime(any())).thenReturn(getRiderJobList());

        Long response = riderApprovalDateService.update();
        assertNotNull(response);
    }

    private List<RiderJobDetails> getRiderJobList() {
        List<RiderJobDetails> list = new ArrayList<RiderJobDetails>();

        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setJobId("s0001");
        riderJobDetails.setJobAcceptedTime(LocalDateTime.now());
        riderJobDetails.setProfileId("rider-id");

        list.add(riderJobDetails);
        return list;
    }

    private Page<RiderProfile> getRiders() {

        List<RiderProfile> list = new ArrayList<>();
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("rider-id");
        riderProfile.setApprovalDateTime(null);

        list.add(riderProfile);
        return new PageImpl<>(list);
    }
}
