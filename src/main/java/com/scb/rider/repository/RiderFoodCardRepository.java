package com.scb.rider.repository;

import com.scb.rider.model.document.RiderFoodCard;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiderFoodCardRepository extends MongoRepository<RiderFoodCard, String> {
    Optional<RiderFoodCard> findByRiderProfileId(String riderProfileId);

    Long deleteByRiderProfileId(String riderId);
}
