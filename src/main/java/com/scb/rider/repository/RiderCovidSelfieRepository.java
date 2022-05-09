package com.scb.rider.repository;

import com.scb.rider.model.document.RiderCovidSelfie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RiderCovidSelfieRepository extends MongoRepository<RiderCovidSelfie, String> {


    Page<RiderCovidSelfie> findByRiderIdAndUploadedTimeBetween(String riderId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Optional<RiderCovidSelfie> findFirstByRiderIdOrderByUploadedTimeDesc(String riderId);

}
