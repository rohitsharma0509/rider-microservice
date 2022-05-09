package com.scb.rider.service.factory;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.enumeration.RiderProfileStage;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BackgroundVerificationUploadService implements UploadService {

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private RiderTrainingAppointmentRepository trainingAppointmentRepository;

    @Override
    public void performOperation(RiderProfile riderProfile) {
        Optional<RiderSelectedTrainingAppointment> riderTraining = trainingAppointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD);
        if(riderTraining.isPresent() && RiderTrainingStatus.COMPLETED.equals(riderTraining.get().getStatus())) {
            riderProfile.setProfileStage(RiderProfileStage.STAGE_3);
            riderProfileRepository.save(riderProfile);
        }
    }
}
