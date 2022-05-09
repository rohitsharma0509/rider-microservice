package com.scb.rider.repository;

import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiderDrivingLicenseDocumentRepository extends MongoRepository<RiderDrivingLicenseDocument, String> {

    Optional<RiderDrivingLicenseDocument> findByRiderProfileId(String riderProfileId);

    RiderDrivingLicenseDocument findByDrivingLicenseNumber(String drivingLicenseNumber);

    Long deleteByRiderProfileId(String riderProfileId);
}
