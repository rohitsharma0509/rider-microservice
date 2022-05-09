package com.scb.rider.service.redis;


import static com.scb.rider.constants.Constants.RIDER_TOKEN_REDIS_CACHE_TTL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import com.scb.rider.config.security.TokenUtils;
import com.scb.rider.exception.AuthenticationException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.redis.RiderAuthDto;
import com.scb.rider.model.redis.RiderTokenCachedEntity;
import com.scb.rider.repository.redis.RiderTokenCacheRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "test")
public class RiderTokenCacheServiceTest {

  
  
  @Mock
  private RiderTokenCacheRepository riderTokenCacheRepository;

  @Mock
  private TokenUtils tokenUtils;
  
  @InjectMocks
  private RiderTokenCacheService riderTokenCacheService;
  
  private static RiderAuthDto riderAuthDto;
  
  private static RiderTokenCachedEntity riderTokenCachedEntity;
  
  @BeforeAll
  static void setUp() {
    
    riderAuthDto = RiderAuthDto.builder().token("asdasdsad213213asdasd==").build();
    riderTokenCachedEntity = RiderTokenCachedEntity.builder()
        .phoneNumber("1100110011").eventId("aaa-bbb-ccc-dddd").loggedOut(false).build();
  }
  
  @Test
  void insertRiderEventIdToRedisTest() throws AuthenticationException {
    
    when(tokenUtils.getClaimFromIdToken(any(), anyString())).thenReturn("1100110011");
    when(riderTokenCacheRepository.save(any())).thenReturn(riderTokenCachedEntity);
    riderTokenCacheService.insertRiderEventIdToRedis(riderAuthDto);
    
  }
  
  @Test
  void logoutRiderFromRedisTest() throws AuthenticationException {
    
    when(riderTokenCacheRepository.findByEventId(anyString())).thenReturn(riderTokenCachedEntity);
    when(tokenUtils.getClaimFromIdToken(any(), anyString())).thenReturn("1100110011");
    when(riderTokenCacheRepository.save(any())).thenReturn(riderTokenCachedEntity);
    riderTokenCacheService.logoutRiderFromRedis(riderAuthDto);
  }
  
  
  @Test
  void validateTokenTest(){
    
    when(riderTokenCacheRepository.findById(anyString())).thenReturn(Optional.of(riderTokenCachedEntity));
    
    boolean isValid = riderTokenCacheService.validateToken("1100110011","aaa-bbb-ccc-dddd");
    assertEquals(isValid, true);
  }

}
