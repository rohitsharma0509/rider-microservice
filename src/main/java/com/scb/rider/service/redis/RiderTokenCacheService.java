package com.scb.rider.service.redis;

import static com.scb.rider.constants.Constants.RIDER_TOKEN_REDIS_CACHE_TTL;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scb.rider.config.security.TokenUtils;
import com.scb.rider.exception.AuthenticationException;
import com.scb.rider.model.redis.RiderAuthDto;
import com.scb.rider.model.redis.RiderTokenCachedEntity;
import com.scb.rider.repository.redis.RiderTokenCacheRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RiderTokenCacheService {

  @Autowired
  private RiderTokenCacheRepository riderTokenCacheRepository;

  @Autowired
  private TokenUtils tokenUtils;



  public void insertRiderEventIdToRedis(RiderAuthDto riderAuthDto) {
    try {
      String eventId = tokenUtils.getClaimFromIdToken(riderAuthDto.getToken(), "event_id");
      String phoneNumber = tokenUtils.getClaimFromIdToken(riderAuthDto.getToken(), "phone_number");
      log.info("TOKEN_FILTER - Insert PhoneNumber:{}, EventId:{}", phoneNumber, eventId);

      RiderTokenCachedEntity riderTokenCachedEntity = RiderTokenCachedEntity.builder()
          .phoneNumber(phoneNumber).eventId(eventId).loggedOut(false).build();

      riderTokenCacheRepository.save(riderTokenCachedEntity);

    } catch (AuthenticationException ex) {
      log.error("Exception Occurred while inserting Token to Redis Exception:{}", ex);
    }
  }



  public void logoutRiderFromRedis(RiderAuthDto riderAuthDto) {
    try {
      String eventId = tokenUtils.getClaimFromIdToken(riderAuthDto.getToken(), "event_id");
      log.info("TOKEN_FILTER - Logout EventId:{}", eventId);
      RiderTokenCachedEntity riderTokenCachedEntity =
          riderTokenCacheRepository.findByEventId(eventId);
      if (Objects.nonNull(riderTokenCachedEntity)) {
        riderTokenCachedEntity.setLoggedOut(true);
        riderTokenCacheRepository.save(riderTokenCachedEntity);
        log.info("TOKEN_FILTER - Succesfully LoggedOut PhoneNumber:{}, EventId:{}",
            riderTokenCachedEntity.getPhoneNumber(), eventId);
      }

    } catch (AuthenticationException ex) {
      log.error("Exception Occurred while logging out Exception:{}", ex);
    }
  }


  public boolean validateToken(String phoneNumber, String eventId) {
    log.info("TOKEN_FILTER - Validating EventId for PhoneNumber:{}, EventId:{}", phoneNumber, eventId);
    Optional<RiderTokenCachedEntity> riderTokenCachedEntity =
        riderTokenCacheRepository.findById(phoneNumber);
    if (riderTokenCachedEntity.isPresent()) {
      RiderTokenCachedEntity riderTokenCached = riderTokenCachedEntity.get();
      if (riderTokenCached.getEventId().equals(eventId) && !riderTokenCached.isLoggedOut()) {
        log.info("TOKEN_FILTER - Rider is Authorized to access API PhoneNumber:{}",
            riderTokenCached.getPhoneNumber());
        return true;
      }
    }
    return false;
  }
}
