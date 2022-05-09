package com.scb.rider.config.security;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.scb.rider.exception.AuthenticationException;
import com.scb.rider.service.redis.RiderTokenCacheService;
import com.scb.rider.util.BeanUtilService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(1)
public class TokenValidationFilter extends HttpFilter {


  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    log.info("Inside Logout Token Validation filter ");

    CachedBodyHttpServletRequest cachedBodyHttpServletRequest =
        new CachedBodyHttpServletRequest(request);
    String accessToken = cachedBodyHttpServletRequest.getHeader("Authorization");
    String idToken = cachedBodyHttpServletRequest.getHeader("idToken");
    Enumeration<String> headers = cachedBodyHttpServletRequest.getHeaderNames();
    while (headers.hasMoreElements()) {
      String headerName = headers.nextElement();
      log.info("Header Name {} and Header Value {}", headerName,
          cachedBodyHttpServletRequest.getHeader(headerName));
    }

    try {
      if (StringUtils.isNotEmpty(cachedBodyHttpServletRequest.getHeader("x-amzn-apigateway-api-id"))
          && !request.getRequestURI().contains("/health") && !isOpsMember(accessToken)) {

        if (StringUtils.isEmpty(idToken) || StringUtils.isEmpty(accessToken)) {
          log.error("idToken or accessToken is missing");
          setForbiddenResponse(response);
          return;
        }
        if (!validateLogoutToken(idToken)) {
          log.info("The Token has been expired or logged out");
          setForbiddenResponse(response);
          return;
        }


      }
    } catch (AuthenticationException ae) {
      log.error("Server Error ", ae.getMessage());
      setForbiddenResponse(response);
      return;
    } catch (TokenExpiredException e) {
      log.error("Server Error : {}", e.getMessage());
      setForbiddenResponse(response);
      return;
    }
    chain.doFilter(cachedBodyHttpServletRequest, response);
  }

  private boolean validateLogoutToken(String idToken) throws AuthenticationException {

    String phoneNumber = getClaimFromToken(idToken, "phone_number");
    String eventId = getClaimFromToken(idToken, "event_id");

    RiderTokenCacheService riderTokenCacheService =
        BeanUtilService.getBean(RiderTokenCacheService.class);

    boolean riderTokenOpt = riderTokenCacheService.validateToken(phoneNumber, eventId);

    if (!riderTokenOpt) {
      return false;
    }

    return true;
  }

  private String getClaimFromToken(String idToken, String claim) throws AuthenticationException {
    TokenUtils tokenUtils = BeanUtilService.getBean(TokenUtils.class);
    return tokenUtils.getClaimFromIdToken(idToken, claim);
  }


  private boolean isOpsMember(String accessToken) throws AuthenticationException {
    TokenUtils tokenUtils = BeanUtilService.getBean(TokenUtils.class);
    boolean isOpsMember = tokenUtils.checkIfOpsToken(accessToken);
    log.info("The access belongs to Ops Member {}", isOpsMember);
    return isOpsMember;
  }

  private void setForbiddenResponse(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 401.
    response.setHeader("message",
        "User is not authorized to perform this operation Token expired or Invalid");
  }
}
