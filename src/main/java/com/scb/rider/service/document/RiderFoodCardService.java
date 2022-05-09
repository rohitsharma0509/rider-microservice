package com.scb.rider.service.document;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.MandatoryFieldMissingException;
import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderFoodCardRequest;
import com.scb.rider.model.dto.RiderFoodCardResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderFoodCardRepository;
import com.scb.rider.repository.RiderProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RiderFoodCardService {

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private RiderFoodCardRepository foodCardRepository;

    public RiderFoodCardResponse addFoodCardDetails(String riderId,
                                                                  RiderFoodCardRequest foodCardRequest) {
        RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));

        Optional<RiderFoodCard> foodCardDocument = foodCardRepository
                .findByRiderProfileId(riderProfile.getId());

        if (foodCardDocument.isPresent()) {
            return RiderFoodCardResponse.of(updateFoodCardDetails(foodCardRequest, foodCardDocument.get()));
        }

        if (MandatoryCheckStatus.REJECTED.equals(foodCardRequest.getStatus())) {
            throw new MandatoryFieldMissingException("Reason is missing for id " + riderId);
        }

        RiderFoodCard mappedDocument = RiderFoodCard.builder()
                .riderProfileId(riderId).status(MandatoryCheckStatus.PENDING)
                .documentUrl(foodCardRequest.getDocumentUrl()).build();
        mappedDocument = foodCardRepository.save(mappedDocument);

        return RiderFoodCardResponse.of(mappedDocument);
    }

    public RiderFoodCard getFoodCardDetailsByProfileId(String riderId) {
        RiderProfile riderProfile = riderProfileRepository.findById(riderId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
        return foodCardRepository.findByRiderProfileId(riderProfile.getId())
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
    }

    public RiderFoodCard updateFoodCardDetails(RiderFoodCardRequest foodCardRequest, RiderFoodCard foodCardDocument) {

        foodCardDocument.setStatus(foodCardRequest.getStatus() != null ? foodCardRequest.getStatus() : foodCardDocument.getStatus());
        foodCardDocument.setDocumentUrl(foodCardRequest.getDocumentUrl() != null ? foodCardRequest.getDocumentUrl() : foodCardDocument.getDocumentUrl());
        foodCardDocument.setReason(foodCardRequest.getReason() !=null ? foodCardRequest.getReason() : foodCardDocument.getReason());
        foodCardDocument.setComment(foodCardRequest.getComment() !=null ? foodCardRequest.getComment(): foodCardDocument.getComment());
        if(MandatoryCheckStatus.REJECTED.equals(foodCardRequest.getStatus())){
            foodCardDocument.setRejectionTime(LocalDateTime.now());
        }
        return foodCardRepository.save(foodCardDocument);
    }

}
