package com.scb.rider.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.scb.rider.model.document.RiderLoginDeviceDetails;

public interface RiderLoginDeviceDetailRepository extends MongoRepository<RiderLoginDeviceDetails, String> {
    
    Optional<RiderLoginDeviceDetails> findByPhoneNumber(String phoneNumber);
}
