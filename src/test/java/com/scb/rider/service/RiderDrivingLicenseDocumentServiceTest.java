package com.scb.rider.service;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderDrivingLicenseDocumentService;
import com.scb.rider.util.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RiderDrivingLicenseDocumentServiceTest {

    private static final String PROFILE_ID = "1";
    private static final String DRIVING_LICENSE_ID = "1";
    private static final String DRIVING_LICENSE_NO = "DL123";
    private static final String NEW_DRIVING_LICENSE_NO = "DL12345";
    private static final String TEST_SMS = "test sms";
    private static final int INVOKED_ONCE = 1;

    @Mock
    private RiderDrivingLicenseDocumentRepository repository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private PropertyUtils propertyUtils;

    @Mock
    private SmsPublisher smsPublisher;

    @InjectMocks
    private RiderDrivingLicenseDocumentService service;

    @Test
    void shouldCreateRiderDrivingLicense() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.save(document)).thenReturn(document);
        when(repository.findByDrivingLicenseNumber(eq(DRIVING_LICENSE_NO))).thenReturn(null);
        RiderDrivingLicenseDocument response = service.createRiderDrivingLicense(document);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
    }

    @Test
    void shouldUpdateRiderDrivingLicenseWithoutValidation() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.save(document)).thenReturn(document);
        RiderDrivingLicenseDocument response = service.updateRiderDrivingLicense(document);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
    }

    @Test
    void shouldFindRiderDrivingLicenseByProfileId() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.of(document));
        Optional<RiderDrivingLicenseDocument> response = service.findRiderDrivingLicenseByProfileId(PROFILE_ID);
        assertEquals(DRIVING_LICENSE_NO, response.get().getDrivingLicenseNumber());
    }

    @Test
    void shouldFindRiderDrivingLicenceByLicenseNo() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.findByDrivingLicenseNumber(eq(DRIVING_LICENSE_NO))).thenReturn(document);
        RiderDrivingLicenseDocument response = service.findRiderDrivingLicenceByLicenseNo(DRIVING_LICENSE_NO);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
    }

    @Test
    void shouldFindRiderDrivingLicenseById() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.findById(eq(DRIVING_LICENSE_ID))).thenReturn(Optional.of(document));
        Optional<RiderDrivingLicenseDocument> response = service.findRiderDrivingLicenseById(DRIVING_LICENSE_ID);
        assertEquals(DRIVING_LICENSE_ID, response.get().getId());
    }
    @Test
    void updateRiderDrivingLicenseThrowExceptionWhenDocAlreadyApprovedNotFound() {
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        document.setStatus(MandatoryCheckStatus.APPROVED);
        RiderProfile rider = new RiderProfile();
        rider.setId(PROFILE_ID);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(rider));
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequest(DRIVING_LICENSE_NO, MandatoryCheckStatus.REJECTED);
        when(repository.save(any(RiderDrivingLicenseDocument.class))).thenReturn(document);
        assertThrows(DocumentAlreadyApprovedException.class, () -> service.updateRiderDrivingLicense(drivingLicenseDocument,document));
    }

    @Test
    void updateRiderDrivingLicenseThrowExceptionWhenRiderNotFound() {
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> service.updateRiderDrivingLicense(null, getRiderDrivingLicenseDocument()));
    }

    @Test
    void shouldUpdateRiderDrivingLicense() {
        RiderProfile rider = new RiderProfile();
        rider.setId(PROFILE_ID);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(rider));
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequest(DRIVING_LICENSE_NO, MandatoryCheckStatus.PENDING);
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.save(any(RiderDrivingLicenseDocument.class))).thenReturn(document);
        RiderDrivingLicenseDocument response = service.updateRiderDrivingLicense(drivingLicenseDocument,document);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
    }

    @Test
    void shouldUpdateRiderDrivingLicenseToRejectedAndSendSmsEvent() {
        RiderProfile rider = new RiderProfile();
        rider.setId(PROFILE_ID);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(rider));
        RiderDrivingLicenseRequest request = getRiderDrivingLicenseRequest(DRIVING_LICENSE_NO, MandatoryCheckStatus.REJECTED);
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.save(any(RiderDrivingLicenseDocument.class))).thenReturn(document);
        when( propertyUtils.getProperty(eq(SmsConstants.DL_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        RiderDrivingLicenseDocument response = service.updateRiderDrivingLicense(request, document);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    void shouldUpdateRiderDrivingLicenseToRejectedWithReasonAndCommentAndSendSmsEvent() {
        RiderProfile rider = new RiderProfile();
        rider.setId(PROFILE_ID);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(rider));
        RiderDrivingLicenseRequest request = getRiderDrivingLicenseRequest(DRIVING_LICENSE_NO, MandatoryCheckStatus.REJECTED);
        request.setReason(Constants.OTHER);
        request.setComment("test Comment");
        request.setTypeOfLicense(null);
        request.setDateOfIssue(null);
        RiderDrivingLicenseDocument document = getRiderDrivingLicenseDocument();
        when(repository.save(any(RiderDrivingLicenseDocument.class))).thenReturn(document);
        when( propertyUtils.getProperty(eq(SmsConstants.DL_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        RiderDrivingLicenseDocument response = service.updateRiderDrivingLicense(request, document);
        assertEquals(DRIVING_LICENSE_NO, response.getDrivingLicenseNumber());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    private RiderDrivingLicenseRequest getRiderDrivingLicenseRequest(String drivingLicenseNo, MandatoryCheckStatus newStatus) {
        return RiderDrivingLicenseRequest.builder()
                .status(newStatus)
                .drivingLicenseNumber(drivingLicenseNo)
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("test")
                .build();
    }

    private RiderDrivingLicenseDocument getRiderDrivingLicenseDocument() {
        return RiderDrivingLicenseDocument.builder()
                .id(DRIVING_LICENSE_ID)
                .status(MandatoryCheckStatus.PENDING)
                .drivingLicenseNumber(DRIVING_LICENSE_NO)
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense("Permanent")
                .riderProfileId(PROFILE_ID).build();
    }
}
