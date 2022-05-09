package com.scb.rider.repository;

import com.scb.rider.model.document.RiderEmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderEmergencyContactRepository extends MongoRepository<RiderEmergencyContact, String> {
    Optional<RiderEmergencyContact> findByProfileId(String profileId);

    Long deleteByProfileId(String profileId);
}
