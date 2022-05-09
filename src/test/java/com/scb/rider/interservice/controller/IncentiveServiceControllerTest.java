package com.scb.rider.interservice.controller;

import com.scb.rider.client.IncentivesServiceFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class IncentiveServiceControllerTest {
    @InjectMocks
    private RiderIncentiveProgressDetailsController riderIncentiveProgressDetailsController;
    @Mock
    private IncentivesServiceFeignClient incentivesServiceFeignClient;

    @Test
    public void getIncentiveProgressTest(){
        when(incentivesServiceFeignClient.getIncentiveProgressDetails(any(String.class),any(String.class))).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getIncentiveProgressDetails("riderId","true");

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getIncentiveTopUpTest(){
        when(incentivesServiceFeignClient.getIncentiveTopUpProgressDetails(any(String.class))).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getIncentiveTopUpProgressDetails("riderId");

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getTopUpHistory(){
        when(incentivesServiceFeignClient.getTopUPIncentiveEarnHistory(any(String.class),anyInt(),anyInt())).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getTopUpIncentiveEarnHistory("riderId",1,1);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getOverTimeHistory(){
        when(incentivesServiceFeignClient.getOverTimeIncentiveEarnHistory(any(String.class),anyInt(),anyInt())).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getOverTimeIncentiveEarnHistory("riderId",1,1);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getTopUpAggregateHistory(){
        when(incentivesServiceFeignClient.getTopUPAggregateIncentiveEarnHistory(any(String.class),anyLong(),anyInt())).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getTopUpAggregateIncentiveEarnHistory("riderId",1L,1);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getOverTimeAggregateEarnHistory(){
        when(incentivesServiceFeignClient.getOverTimeAggregateIncentiveEarnHistory(any(String.class),anyLong(),anyInt())).thenReturn(new Object());
        ResponseEntity<?> responseEntity = riderIncentiveProgressDetailsController
                .getOverTimeAggregateIncentiveEarnHistory("riderId",1L,1);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
