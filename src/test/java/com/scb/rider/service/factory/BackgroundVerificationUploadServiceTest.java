package com.scb.rider.service.factory;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackgroundVerificationUploadServiceTest {

    private static final String PROFILE_ID = "609a5be7a769cc6adc3cbc98";
    private static final int ONE_TIME = 1;

    @InjectMocks
    private BackgroundVerificationUploadService backgroundVerificationUploadService;

    @Mock
    private RiderTrainingAppointmentRepository trainingAppointmentRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Test
    void shouldNotUpdateRiderProfileStageWhenTrainingNotCompleted() {
        when(trainingAppointmentRepository.findByRiderIdAndTrainingType(eq(PROFILE_ID), eq(TrainingType.FOOD))).thenReturn(Optional.empty());
        backgroundVerificationUploadService.performOperation(getRiderProfile());
        verifyZeroInteractions(riderProfileRepository);
    }

    @Test
    void shouldUpdateRiderProfileStageWhenTrainingCompleted() {
        RiderSelectedTrainingAppointment training = RiderSelectedTrainingAppointment.builder()
                .riderId(PROFILE_ID).status(RiderTrainingStatus.COMPLETED).build();
        when(trainingAppointmentRepository.findByRiderIdAndTrainingType(eq(PROFILE_ID), eq(TrainingType.FOOD))).thenReturn(Optional.of(training));
        backgroundVerificationUploadService.performOperation(getRiderProfile());
        verify(riderProfileRepository, times(ONE_TIME)).save(any(RiderProfile.class));
    }

    private RiderProfile getRiderProfile() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(PROFILE_ID);
        return riderProfile;
    }
}
