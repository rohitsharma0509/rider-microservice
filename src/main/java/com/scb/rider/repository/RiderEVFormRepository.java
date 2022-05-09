package com.scb.rider.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scb.rider.model.document.RiderEVForm;

public interface RiderEVFormRepository extends MongoRepository<RiderEVForm, String> {

	Optional<RiderEVForm> findByRiderProfileId(String riderProfileId);

}
