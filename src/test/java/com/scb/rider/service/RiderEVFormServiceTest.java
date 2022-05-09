package com.scb.rider.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderEVForm;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEVFormDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderEVFormRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.service.document.RiderEVFormService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderEVFormServiceTest {

	@Mock
	private RiderProfileRepository riderProfileRepository;

	@Mock
	private RiderEVFormRepository evFormRepository;

	@InjectMocks
	private RiderEVFormService evFormService;

	private static RiderEVForm evFormPending;
	private static RiderEVForm evFormApprove;
	private static RiderEVForm evFormReject;
	private static RiderEVFormDto evFormDto;
	private static RiderProfile riderProfile;
	@Mock
	private RiderProfileUpdaterService riderProfileUpdaterService;
    
	
	@BeforeAll
	static void setup() {
		riderProfile = new RiderProfile();
		riderProfile.setId("123456789");

		evFormApprove = RiderEVForm.builder().riderProfileId(riderProfile.getId()).status(MandatoryCheckStatus.APPROVED)
				.documentUrl("ftp://test.com/path").evRentalAgreementNumber("EV10123").province("Bangkok")
				.build();
		evFormPending = RiderEVForm.builder().riderProfileId(riderProfile.getId()).status(MandatoryCheckStatus.PENDING)
				.documentUrl("ftp://test.com/path").evRentalAgreementNumber("EV10123").province("Bangkok")
				.build();
		evFormReject = RiderEVForm.builder().riderProfileId(riderProfile.getId()).status(MandatoryCheckStatus.REJECTED)
				.documentUrl("ftp://test.com/path").evRentalAgreementNumber("EV10123").province("Bangkok")
				.reason("reason").comment("comment").rejectionTime(LocalDateTime.now())
				.build();

		evFormDto = RiderEVFormDto.builder().riderProfileId(riderProfile.getId()).status(MandatoryCheckStatus.PENDING)
				.documentUrl("ftp://test.com/path").build();
	}

	@Test
	@Order(1)
	void test_CreateRiderEVForm() {
		when(riderProfileRepository.findById(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
		when(evFormRepository.findByRiderProfileId(Mockito.anyString())).thenReturn(Optional.empty());
		when(evFormRepository.save(Mockito.any())).thenReturn(evFormPending);

		RiderEVForm newRiderEVForm = evFormService.saveRiderEVForm(evFormDto);

		assertNotNull(newRiderEVForm);
		assertEquals("123456789", newRiderEVForm.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.PENDING, newRiderEVForm.getStatus());
	}

	@Test
	@Order(2)
	void test_GetRiderEVForm() {
		when(evFormRepository.findByRiderProfileId(Mockito.anyString())).thenReturn(Optional.of(evFormPending));
		RiderEVForm newRiderEVForm = evFormService.getRiderEVForm("123456789");

		assertNotNull(newRiderEVForm);
		assertEquals("123456789", newRiderEVForm.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.PENDING, newRiderEVForm.getStatus());
	}

	@Test
	@Order(3)
	void test_approveRiderEVForm() {
		evFormDto.setStatus(MandatoryCheckStatus.APPROVED);
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("123");
		RiderPreferredZones zones = new RiderPreferredZones();
		zones.setPreferredZoneId("1");
		riderProfile.setRiderPreferredZones(zones);
		riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
		riderProfile.setStatus(RiderStatus.AUTHORIZED);
		when(riderProfileRepository.findById(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
		when(evFormRepository.findByRiderProfileId(Mockito.anyString())).thenReturn(Optional.of(evFormApprove));
		when(evFormRepository.save(Mockito.any())).thenReturn(evFormApprove);

		RiderEVForm newRiderEVForm = evFormService.saveRiderEVForm(evFormDto);

		assertNotNull(newRiderEVForm);
		assertEquals("123456789", newRiderEVForm.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, newRiderEVForm.getStatus());
		assertNull(newRiderEVForm.getRejectionTime());
		}
	
	@Test
	@Order(4)
	void test_rejectRiderEVForm() {
		evFormDto.setStatus(MandatoryCheckStatus.REJECTED);
		evFormDto.setProvince("Bangkok");
		evFormDto.setEvRentalAgreementNumber("EV10123");
		evFormDto.setComment("comment");
		evFormDto.setDocumentUrl("ftp://test.com/path");
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("123");
		RiderPreferredZones zones = new RiderPreferredZones();
		zones.setPreferredZoneId("1");
		riderProfile.setRiderPreferredZones(zones);
		riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
		riderProfile.setStatus(RiderStatus.AUTHORIZED);
		when(riderProfileRepository.findById(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
		when(evFormRepository.findByRiderProfileId(Mockito.anyString())).thenReturn(Optional.of(evFormReject));
		when(evFormRepository.save(Mockito.any())).thenReturn(evFormReject);

		RiderEVForm newRiderEVForm = evFormService.saveRiderEVForm(evFormDto);

		assertNotNull(newRiderEVForm);
		assertEquals("123456789", newRiderEVForm.getRiderProfileId());
		assertEquals(MandatoryCheckStatus.REJECTED, newRiderEVForm.getStatus());
		assertNotNull(newRiderEVForm.getRejectionTime());
		assertNotNull(newRiderEVForm.getReason());
		}

	@Test
	void test_GetRiderEVForm_NotFound() {
		when(evFormRepository.findByRiderProfileId(Mockito.anyString())).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> evFormService.getRiderEVForm("123456789"));
	}

	@Test
	void test_SaveRiderEVForm_NotFound() {
		when(riderProfileRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> evFormService.saveRiderEVForm(evFormDto));
	}
}
