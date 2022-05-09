package com.scb.rider.repository;

import com.scb.rider.model.document.RiderSuspendHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RiderSuspendHistoryRepository extends MongoRepository<RiderSuspendHistory, String> {

    Page<RiderSuspendHistory> findByRiderIdAndCreatedDateAfter(String riderId, LocalDateTime createdDateAfter, Pageable pageable);

    Optional<RiderSuspendHistory> findByRiderId(String riderId);
}
