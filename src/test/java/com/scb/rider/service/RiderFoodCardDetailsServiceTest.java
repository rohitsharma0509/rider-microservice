package com.scb.rider.service;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderFoodCardRequest;
import com.scb.rider.model.dto.RiderFoodCardResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderFoodCardRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderFoodCardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderFoodCardDetailsServiceTest {

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private RiderFoodCardRepository riderFoodCardRepository;

    @InjectMocks
    private RiderFoodCardService riderFoodCardService;

    private static final String RIDER_ID = "12314";

    @Test
    void throwExceptionAddFoodCardDetailsWhenRiderNotExist() {
        when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class,
                () -> riderFoodCardService.addFoodCardDetails(RIDER_ID, getFoodCardRequestData(MandatoryCheckStatus.PENDING)));
    }

    @Test
    void shouldAddBFoodCardDetailsWhenRiderExist() {
        when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
        when(riderFoodCardRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.empty());
        when(riderFoodCardRepository.save(any(RiderFoodCard.class))).thenReturn(getFoodCardDocumentData(MandatoryCheckStatus.APPROVED));
        RiderFoodCardResponse result = riderFoodCardService.addFoodCardDetails(RIDER_ID, getFoodCardRequestData(MandatoryCheckStatus.APPROVED));
        assertNotNull(result);
        assertEquals(RIDER_ID, result.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
    }

    @Test
    void throwExceptionFindFoodCardDetailsWhenRiderNotExist() {
        when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class,
                () -> riderFoodCardService.getFoodCardDetailsByProfileId(RIDER_ID));
    }

    @Test
    void shouldGetFoodCardByProfileIdWhenBackgroundDetailsExist() {
        when(riderProfileRepository.findById(RIDER_ID)).thenReturn(Optional.of(getRiderProfile()));
        when(riderFoodCardRepository.findByRiderProfileId(RIDER_ID)).thenReturn(Optional.of(getFoodCardDocumentData(MandatoryCheckStatus.APPROVED)));
        RiderFoodCard result = riderFoodCardService.getFoodCardDetailsByProfileId(RIDER_ID);
        assertEquals(RIDER_ID, result.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
    }

    @Test
    void shouldUpdateBackgroundVerificationDetailsWithRejectedStatusWithoutReason() {
        when(riderFoodCardRepository.save(any(RiderFoodCard.class))).thenReturn(getFoodCardDocumentData(MandatoryCheckStatus.APPROVED));
        RiderFoodCard result = riderFoodCardService.updateFoodCardDetails(getFoodCardRequestData(MandatoryCheckStatus.APPROVED), getFoodCardDocumentData(MandatoryCheckStatus.PENDING));
        assertEquals(RIDER_ID, result.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.APPROVED, result.getStatus());
    }

    private static RiderFoodCard getFoodCardDocumentData(MandatoryCheckStatus status) {
        return RiderFoodCard.builder().id("1")
                .riderProfileId(RIDER_ID)
                .status(status)
                .documentUrl("localhost/").build();
    }

    private RiderProfile getRiderProfile() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(RIDER_ID);
        return riderProfile;
    }

    private static RiderFoodCardRequest getFoodCardRequestData(MandatoryCheckStatus status) {
        return RiderFoodCardRequest.builder()
                .status(status)
                .documentUrl("localhost/").build();
    }
}
