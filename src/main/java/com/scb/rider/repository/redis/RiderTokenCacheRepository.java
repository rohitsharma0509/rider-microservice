package com.scb.rider.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.scb.rider.model.redis.RiderTokenCachedEntity;

@Repository
public interface RiderTokenCacheRepository extends CrudRepository<RiderTokenCachedEntity, String> {
    RiderTokenCachedEntity findByPhoneNumber(String eventId);
    RiderTokenCachedEntity findByEventId(String eventId);
}