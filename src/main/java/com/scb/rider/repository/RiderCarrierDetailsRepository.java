package com.scb.rider.repository;

import com.scb.rider.model.document.RiderCarrierDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiderCarrierDetailsRepository extends MongoRepository<RiderCarrierDetails, String> {
    RiderCarrierDetails findByRiderId(String riderId);
}
