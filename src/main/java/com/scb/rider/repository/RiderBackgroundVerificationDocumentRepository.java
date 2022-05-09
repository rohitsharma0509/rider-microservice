package com.scb.rider.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scb.rider.model.document.RiderBackgroundVerificationDocument;

public interface RiderBackgroundVerificationDocumentRepository extends MongoRepository<RiderBackgroundVerificationDocument, String> {

	Optional<RiderBackgroundVerificationDocument> findByRiderProfileId(String riderProfileId);

	Long deleteByRiderProfileId(String riderId);
}
