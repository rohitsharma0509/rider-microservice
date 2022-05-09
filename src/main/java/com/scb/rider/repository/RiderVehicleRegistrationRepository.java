package com.scb.rider.repository;

import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiderVehicleRegistrationRepository extends MongoRepository<RiderVehicleRegistrationDocument, String> {
    Optional<RiderVehicleRegistrationDocument> findByRiderProfileId(String riderProfileId);
    RiderVehicleRegistrationDocument findByRegistrationNo(String regNo);
    RiderVehicleRegistrationDocument findByRegistrationNoAndProvince(String regNo,String province);
    Long deleteByRiderProfileId(String riderProfileId);
}