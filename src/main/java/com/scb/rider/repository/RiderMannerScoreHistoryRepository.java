package com.scb.rider.repository;

import com.scb.rider.model.document.RiderMannerScoreHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RiderMannerScoreHistoryRepository extends MongoRepository<RiderMannerScoreHistory, String> {

    Page<RiderMannerScoreHistory> findByRiderIdAndCreatedDateAfter(String riderId, LocalDateTime createdDateAfter, Pageable pageable);

    Optional<RiderMannerScoreHistory> findByRiderId(String riderId);
}
