package com.scb.rider.repository;

import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.enumeration.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiderDeviceDetailRepository extends MongoRepository<RiderDeviceDetails, String> {

    Optional<RiderDeviceDetails> findByProfileIdAndPlatform(String riderId, Platform platform);

    Optional<RiderDeviceDetails> findByProfileId(String riderId);
}
