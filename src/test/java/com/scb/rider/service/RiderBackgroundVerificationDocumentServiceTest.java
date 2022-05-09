package com.scb.rider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.exception.MandatoryFieldMissingException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import com.scb.rider.util.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderBackgroundVerificationDocumentRepository;
import com.scb.rider.service.document.RiderBackgroundVerificationDocumentService;

@ExtendWith(MockitoExtension.class)
class RiderBackgroundVerificationDocumentServiceTest {

	@Mock
	private RiderProfileRepository riderProfileRepository;

	@Mock
	private RiderBackgroundVerificationDocumentRepository riderBackGroundRepository;

	@Mock
	private PropertyUtils propertyUtils;

	@Mock
	private SmsPublisher smsPublisher;

	@Mock
	private RiderUploadedDocumentService riderUploadedDocumentService;

	@InjectMocks
	private RiderBackgroundVerificationDocumentService riderBackGroundService;

	private static final String EMPTY = "";
	private static final String RIDER_ID = "12314";
	private static final String TEST_REASON = "test reason";
	private static final String TEST_SMS = "test sms";
	private static final int INVOKED_ONCE = 1;

	@Test
	void throwExceptionAddBackgroundVerificationDetailsWhenRiderNotExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> riderBackGroundService.addBackgroundVerificationDetails(
				RIDER_ID, getBackgroundRequestData(MandatoryCheckStatus.PENDING, EMPTY, LocalDate.now()), Constants.OPS));
		verifyZeroInteractions(riderUploadedDocumentService);
	}

	@Test
	void throwExceptionAddBackgroundVerificationDetailsWithRejectingStatusWithoutReason() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.empty());
		assertThrows(MandatoryFieldMissingException.class, () -> riderBackGroundService.addBackgroundVerificationDetails(
				RIDER_ID, getBackgroundRequestData(MandatoryCheckStatus.REJECTED, EMPTY, null), Constants.OPS));
		verifyZeroInteractions(riderUploadedDocumentService);
	}

	@Test
	void shouldAddBackgroundVerificationDetailsWhenRiderExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.empty());
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(getBackgroundDocumentData(MandatoryCheckStatus.APPROVED));
		RiderBackgroundVerificationDetailsResponse result = riderBackGroundService.addBackgroundVerificationDetails(RIDER_ID,
				getBackgroundRequestData(MandatoryCheckStatus.APPROVED, TEST_REASON, null), Constants.OPS);
		assertNotNull(result);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
	}

	@Test
	void throwExceptionAddBackgroundVerificationDetailsWithPendingStatusWithoutDueDate() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.of(getBackgroundDocumentData(MandatoryCheckStatus.REJECTED)));
		assertThrows(MandatoryFieldMissingException.class, () -> riderBackGroundService.addBackgroundVerificationDetails(
				RIDER_ID, getBackgroundRequestData(MandatoryCheckStatus.PENDING, EMPTY, null), Constants.OPS));
	}

	@Test
	void throwExceptionGetBackgroundVerificationDetailsByProfileIdWhenRiderNotExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> riderBackGroundService.getBackgroundVerificationDetailsByProfileId(RIDER_ID));
	}

	@Test
	void throwExceptionGetBackgroundVerificationDetailsByProfileIdWhenBackgroundDetailsNotExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class,
				() -> riderBackGroundService.getBackgroundVerificationDetailsByProfileId(RIDER_ID));
	}

	@Test
	void shouldGetBackgroundVerificationDetailsByProfileIdWhenBackgroundDetailsNotExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.of(getBackgroundDocumentData(MandatoryCheckStatus.APPROVED)));
		RiderBackgroundVerificationDocument result = riderBackGroundService.getBackgroundVerificationDetailsByProfileId(RIDER_ID);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
	}
	@Test
	void updateBackgroundVerificationDetailsThrowExceptionWhenDocAlreadyApproved() {
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.APPROVED);
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.REJECTED, EMPTY, null);
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		assertThrows(DocumentAlreadyApprovedException.class, () -> riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.OPS));
	}
	@Test
	void updateBackgroundVerificationDetailsThrowExceptionWhenRiderNotExist() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.empty());
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.PENDING, EMPTY, null);
		RiderBackgroundVerificationDocument document = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		assertThrows(DataNotFoundException.class, () -> riderBackGroundService.updateBackgroundVerificationDetails(request, document, Constants.OPS));
	}

	@Test
	void throwExceptionUpdateBackgroundVerificationDetailsWithPendingStatusWithoutDueDate() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.PENDING, EMPTY, null);
		RiderBackgroundVerificationDocument document = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		assertThrows(MandatoryFieldMissingException.class, () -> riderBackGroundService.updateBackgroundVerificationDetails(request, document, Constants.OPS));
	}

	@Test
	void shouldUpdateBackgroundVerificationStatusToPendingWithoutDueDateWhenRequestedByRider() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.PENDING, EMPTY, null);
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		RiderBackgroundVerificationDocument updatedDocument = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(updatedDocument);
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		RiderBackgroundVerificationDocument result = riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.RIDER);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.PENDING, result.getStatus());
	}

	@Test
	void throwExceptionUpdateBackgroundVerificationDetailsWithRejectedStatusWithoutReason() {
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.REJECTED, EMPTY, null);
		RiderBackgroundVerificationDocument document = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		assertThrows(MandatoryFieldMissingException.class, () -> riderBackGroundService.updateBackgroundVerificationDetails(request, document, Constants.OPS));
	}

	@Test
	void shouldUpdateBackgroundVerificationDetails() {
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		RiderBackgroundVerificationDocument updatedDocument = getBackgroundDocumentData(MandatoryCheckStatus.APPROVED);
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.APPROVED, EMPTY, null);
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(updatedDocument);
		RiderBackgroundVerificationDocument result = riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.OPS);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
	}

	@Test
	void shouldNotSendSmsWhenDocumentIsAlreadyRejected() {
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		RiderBackgroundVerificationDocument updatedDocument = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.REJECTED, TEST_REASON, null);
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(updatedDocument);
		RiderBackgroundVerificationDocument result = riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.OPS);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.REJECTED, result.getStatus());
		verifyZeroInteractions(smsPublisher);
	}

	@Test
	void shouldUpdateBackgroundVerificationStatusToRejectedAndSendSmsEvent() {
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		RiderBackgroundVerificationDocument updatedDocument = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.REJECTED, TEST_REASON, null);
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(updatedDocument);
		when(propertyUtils.getProperty(eq(SmsConstants.BGV_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
		RiderBackgroundVerificationDocument result = riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.OPS);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.REJECTED, result.getStatus());
		verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
	}

	@Test
	void shouldUpdateBackgroundVerificationStatusToRejectedWithOtherReasonAndCommentAndSendSmsEvent() {
		RiderBackgroundVerificationDocument existingDocument = getBackgroundDocumentData(MandatoryCheckStatus.PENDING);
		RiderBackgroundVerificationDocument updatedDocument = getBackgroundDocumentData(MandatoryCheckStatus.REJECTED);
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData(MandatoryCheckStatus.REJECTED, Constants.OTHER, null);
		request.setComment("test comment");
		when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
		when(riderBackGroundRepository.save(any(RiderBackgroundVerificationDocument.class))).thenReturn(updatedDocument);
		when(propertyUtils.getProperty(eq(SmsConstants.BGV_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
		RiderBackgroundVerificationDocument result = riderBackGroundService.updateBackgroundVerificationDetails(request, existingDocument, Constants.OPS);
		assertEquals(RIDER_ID, result.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.REJECTED, result.getStatus());
		verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
	}

	private RiderProfile getRiderProfile() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId(RIDER_ID);
		return riderProfile;
	}

	private static RiderBackgroundVerificationDocument getBackgroundDocumentData(MandatoryCheckStatus status) {
		return RiderBackgroundVerificationDocument.builder().id("1")
				.riderProfileId(RIDER_ID).status(status).dueDate(LocalDate.of(2022, 12, 11))
				.reason("Test").documentUrls(Arrays.asList("localhost/")).build();
	}

	private static RiderBackgroundVerificationDetailsRequest getBackgroundRequestData(MandatoryCheckStatus status, String reason, LocalDate dueDate) {
		return RiderBackgroundVerificationDetailsRequest.builder()
				.status(status).dueDate(dueDate).reason(reason)
				.documentUrls(Arrays.asList("localhost/")).build();
	}

}
