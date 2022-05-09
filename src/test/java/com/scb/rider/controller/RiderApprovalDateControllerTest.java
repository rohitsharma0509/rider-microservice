package com.scb.rider.controller;

import com.scb.rider.service.RiderApprovalDateService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderApprovalDateControllerTest {

    @InjectMocks
    private RiderApprovalDateController riderApprovalDateController;

    @Mock
    private RiderApprovalDateService riderApprovalDateService;
    @Test
    public void getRiderTierDetailsForDownloadTest() throws Exception {
        when(riderApprovalDateService.update()).thenReturn(0L);
        ResponseEntity response = riderApprovalDateController.updateApprovalDate();
        assertTrue(ObjectUtils.isNotEmpty(response));
    }
}
