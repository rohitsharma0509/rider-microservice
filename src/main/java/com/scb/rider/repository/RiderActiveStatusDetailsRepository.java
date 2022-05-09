package com.scb.rider.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scb.rider.model.document.RiderActiveStatusDetails;

public interface RiderActiveStatusDetailsRepository extends MongoRepository<RiderActiveStatusDetails, String> {

	Optional<RiderActiveStatusDetails> findByRiderId(String riderId);

   
}
