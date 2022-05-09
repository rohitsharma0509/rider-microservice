package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.service.document.RiderDrivingLicenseDocumentService;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderDrivingLicenseControllerTest {

    @InjectMocks
    private RiderDrivingLicenseController drivingLicenseController;

    @Mock
    private RiderDrivingLicenseDocumentService drivingLicenseDocumentService;

    @Test
    public void testCreateRiderDrivingLicenseRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber("1234")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.createRiderDrivingLicense(any(RiderDrivingLicenseDocument.class)))
                .thenReturn(riderDrivingLicenseDocument);


        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .createRiderDrivingLicenseDetails(Constants.OPS_MEMBER, "1",drivingLicenseDocument);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(drivingLicenseDocument.toString());

    }

    @Test
    public void testCreateRiderDrivingLicenseRequestRiderRpofileAlredayExist() throws Exception {

        // prepare data and mock's behaviour
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber("1234")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.createRiderDrivingLicense(any(RiderDrivingLicenseDocument.class)))
                .thenReturn(riderDrivingLicenseDocument);

        when(drivingLicenseDocumentService.updateRiderDrivingLicense(any(),any())).thenReturn(riderDrivingLicenseDocument);
        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class)))
                .thenReturn(Optional.of(riderDrivingLicenseDocument));

        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .createRiderDrivingLicenseDetails(Constants.OPS_MEMBER, "1",drivingLicenseDocument);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(drivingLicenseDocument.toString());

    }

    @Test
    public void testUpdateRiderDrivingLicenseRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber("1234")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class))).thenReturn(
                Optional.of(riderDrivingLicenseDocument));

        when(drivingLicenseDocumentService.updateRiderDrivingLicense(any(RiderDrivingLicenseRequest.class),
                any(RiderDrivingLicenseDocument.class))).thenReturn(riderDrivingLicenseDocument);

        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .updateRiderDrivingLicenseDetails(Constants.OPS_MEMBER, "1",drivingLicenseDocument);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
    }

    @Test
    public void testNotFoundThrowExceptionWhenProfileNotFound() throws Exception {

        // prepare data and mock's behaviour
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber("1234")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .getDrivingLicenseProfileById("1");

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());

    }

    @Test
    public void testGetRiderDrivingLicenseResponseByIdSuccess() throws Exception {
        // prepare data and mock's behaviour

        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseById(any(String.class)))
                .thenReturn(Optional.of(riderDrivingLicenseDocument));


        // verify
        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .getDrivingLicenseById("1");

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());

    }

    @Test
    public void testGetRiderDrivingLicenseResponseByIdNotFound() throws Exception {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class))).thenReturn(
                Optional.empty());

        when(drivingLicenseDocumentService.findRiderDrivingLicenseById(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .getDrivingLicenseById("1");

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());

    }
    @Test
    public void testGetRiderDrivingLicenseResponseByProfileIdSuccess() throws Exception {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class)))
                .thenReturn(Optional.of(riderDrivingLicenseDocument));


        // verify
        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .getDrivingLicenseProfileById("1");

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
    }

    @Test
    public void testGetRiderDrivingLicenseResponseByProfileIdNotFound() throws Exception {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .id("id-1")
                .drivingLicenseNumber("1234")
                .riderProfileId("1")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        when(drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderDrivingLicenseResponse> apiResponse = drivingLicenseController
                .getDrivingLicenseProfileById("1");

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());


    }

}
