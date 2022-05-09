package com.scb.rider.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scb.rider.config.security.TokenUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Disabled
public class TestTokenUtils {

  private TokenUtils tokenUtils;

  @Test
  public void testTokenParsingToGetPhNo() throws JsonProcessingException {
    String idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        + ".eyJzdWIiOiIyYjJjZjNkYS1hNGY4LTRmYmUtODY2Yi0xMTI2OTk1MDY2MWUiLCJhdWQiOiI1N28wMDV1bzloN3A5OWliZjJoaGtoN2xyZSIsImV2ZW50X2lkIjoiNzk2MDg5MzEtYTU4Yy00ZGQ0LTlmMmQtMmVjYzMxOTI3MGU3IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2MTI4NTA2MTQsImlzcyI6Imh0dHBzOi8vY29nbml0by1pZHAuYXAtc291dGhlYXN0LTEuYW1hem9uYXdzLmNvbS9hcC1zb3V0aGVhc3QtMV9ITERXTFVEa20iLCJjb2duaXRvOnVzZXJuYW1lIjoiMmIyY2YzZGEtYTRmOC00ZmJlLTg2NmItMTEyNjk5NTA2NjFlIiwicGhvbmVfbnVtYmVyIjoiKzkxMTIzNDU2Nzg5MCIsImN1c3RvbTpTZWNyZXQiOiI1ZDgzNTNlMzVhYzdkMmY1IiwiZXhwIjoxNjEyOTM3MDE0LCJpYXQiOjE2MTI4NTA2MTR9"
        + ".4NaqjmK24C_qLZAGYoLB0wuzEAN1Cf20y1GoRLdqRTY";
    //String phoneNumber = tokenUtils.getPhoneNumberFromIdToken(idToken);

    //assertEquals("+919650654885", phoneNumber);
  }

  @Test
  public void testValidateToken() throws JsonProcessingException {
    String idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        + ".eyJzdWIiOiIyYjJjZjNkYS1hNGY4LTRmYmUtODY2Yi0xMTI2OTk1MDY2MWUiLCJhdWQiOiI1N28wMDV1bzloN3A5OWliZjJoaGtoN2xyZSIsImV2ZW50X2lkIjoiNzk2MDg5MzEtYTU4Yy00ZGQ0LTlmMmQtMmVjYzMxOTI3MGU3IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2MTI4NTA2MTQsImlzcyI6Imh0dHBzOi8vY29nbml0by1pZHAuYXAtc291dGhlYXN0LTEuYW1hem9uYXdzLmNvbS9hcC1zb3V0aGVhc3QtMV9ITERXTFVEa20iLCJjb2duaXRvOnVzZXJuYW1lIjoiMmIyY2YzZGEtYTRmOC00ZmJlLTg2NmItMTEyNjk5NTA2NjFlIiwicGhvbmVfbnVtYmVyIjoiKzkxMTIzNDU2Nzg5MCIsImN1c3RvbTpTZWNyZXQiOiI1ZDgzNTNlMzVhYzdkMmY1IiwiZXhwIjoxNjEyOTM3MDE0LCJpYXQiOjE2MTI4NTA2MTR9"
        + ".4NaqjmK24C_qLZAGYoLB0wuzEAN1Cf20y1GoRLdqRTY";
    tokenUtils.validateIdToken(idToken);

  }



}
