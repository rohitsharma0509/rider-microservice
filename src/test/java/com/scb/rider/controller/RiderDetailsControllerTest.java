package com.scb.rider.controller;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderDetailsDto;
import com.scb.rider.service.RiderDetailsService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderDetailsControllerTest {

    @InjectMocks
    private RiderDetailsController riderDetailsController;

    @Mock
    private RiderDetailsService riderDetailsService;

    private static RiderDetailsDto riderDetailsDto;

    @BeforeAll
    static void setUp() {
    }

    @Test
    void shouldReturnById() {
        when(riderDetailsService.getRiderDetailsById(anyString(),  any()))
        .thenReturn(RiderDetailsDto.builder().build());
        ResponseEntity<RiderDetailsDto> fetchedResult = riderDetailsController.getRiderDetailsById(anyString(), any());
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getBody()));
        assertEquals(HttpStatus.OK, fetchedResult.getStatusCode());
        assertNotNull(fetchedResult.getBody().toString());
    }

    @Test
    void shouldReturnByPhoneNumber() {
        when(riderDetailsService.getRiderDetailsByPhoneNumber("12345678")).thenReturn(RiderDetailsDto.builder().build());
        ResponseEntity<RiderDetailsDto> fetchedResult = riderDetailsController.getRiderDetailsByPhoneNumber("12345678");
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getBody()));
        assertEquals(HttpStatus.OK, fetchedResult.getStatusCode());
        assertNotNull(fetchedResult.getBody().toString());
    }

    @Test
    void shouldReturnDetailsWithDocumentsById() {
        when(riderDetailsService.getRiderDocumentDetails("12345678")).thenReturn(RiderDetailsDto.builder().build());
        ResponseEntity<RiderDetailsDto> fetchedResult = riderDetailsController.getRiderDocsDetails("12345678");
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getBody()));
        assertEquals(HttpStatus.OK, fetchedResult.getStatusCode());
        assertNotNull(fetchedResult.getBody().toString());
    }

    @Test
    void deleteRiderProfileByMobileNum() throws Exception {
        String mobileNum = "12345678";
        when(riderDetailsService.deleteRiderProfileByMobileNumber(mobileNum)).thenReturn(new RiderProfile());
        ResponseEntity<?> fetchedResult = riderDetailsController.deleteRiderProfile(mobileNum);
        assertEquals(HttpStatus.OK, fetchedResult.getStatusCode());
    }
}