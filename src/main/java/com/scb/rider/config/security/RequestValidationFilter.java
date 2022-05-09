package com.scb.rider.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.ApiError;
import com.scb.rider.exception.AuthenticationException;
import com.scb.rider.kafka.RiderAvailabilityKafkaPublisher;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.service.document.RiderLoginDeviceService;
import com.scb.rider.service.document.RiderSearchService;
import com.scb.rider.util.BeanUtilService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(0)
public class RequestValidationFilter extends HttpFilter {

  private static final String DEVICE_ID_INVALID_LOGOUT = "DEVICE_ID_INVALID_LOGOUT";

  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    log.info("Inside request filter");

    CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
    String accessToken = cachedBodyHttpServletRequest.getHeader("Authorization");
    String idToken = cachedBodyHttpServletRequest.getHeader("idToken");
    Enumeration<String> headers = cachedBodyHttpServletRequest.getHeaderNames();
    while ( headers.hasMoreElements()){
      String headerName = headers.nextElement();
      log.info("Header Name {} and Header Value {}", headerName, cachedBodyHttpServletRequest.getHeader(headerName));
    }

    try {
      if (StringUtils.isNotEmpty(cachedBodyHttpServletRequest.getHeader("x-amzn-apigateway-api-id")) && !request.getRequestURI().contains("/health") && !isOpsMember(accessToken)) {

        if (StringUtils.isEmpty(idToken) || StringUtils.isEmpty(accessToken)) {
          log.error("idToken or accessToken is missing");
          setForbiddenResponse(response);
          return;
        }
        if (!validateAccessIdToken(idToken, accessToken)){
          log.info("Id token and auth token does not match");
          throw new AuthenticationException("Unauthorised");
        }

        log.info("Retrieving the message body");
        String body = cachedBodyHttpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        log.info("Retrieved message body");
        String fullPhoneNumber = getPhoneNumberUsingToken(idToken);
        if(StringUtils.isBlank(fullPhoneNumber)) {
          log.info("phone number from idToken is not valid");
          return;
        }
        String phoneNumber = fullPhoneNumber.substring(3);
        
        if (!validatePathParamsRequest(response, cachedBodyHttpServletRequest, phoneNumber)) {
          log.info("Path params in request are not valid");
          return;
        }

        if ( (!request.getRequestURI().contains("/upload")) && (request.getMethod().equals("POST") || request.getMethod().equals("PUT"))
                && !validateRequestBody(response, body, phoneNumber, cachedBodyHttpServletRequest)) {
          return;
        }
        

        
      }
    } catch (JsonProcessingException jpe) {
      log.warn("Exception in parsing input json");
      log.info("Request body is not in json format");
    } catch(AuthenticationException ae){
      log.error("Server Error " , ae.getMessage());
      setForbiddenResponse(response);
      return;
    }
    setXUserIdAttribute(cachedBodyHttpServletRequest);
    chain.doFilter(cachedBodyHttpServletRequest, response);
  }

  public void setXUserIdAttribute(CachedBodyHttpServletRequest cachedBodyHttpServletRequest) {
    if(ObjectUtils.isNotEmpty(cachedBodyHttpServletRequest.getAttribute(Constants.X_USER_ID))) {
      log.info("requested from app, setting x-user-id in request attribute from request attribute");
      cachedBodyHttpServletRequest.setAttribute(Constants.X_USER_ID, cachedBodyHttpServletRequest.getAttribute(Constants.X_USER_ID));
    } else {
      log.info("request from ops-portal, setting x-user-id in request attribute from request header");
      String xUserId =  StringUtils.isNotBlank(cachedBodyHttpServletRequest.getHeader(Constants.X_USER_ID)) ?
              cachedBodyHttpServletRequest.getHeader(Constants.X_USER_ID) : Constants.OPS_MEMBER;
      cachedBodyHttpServletRequest.setAttribute(Constants.X_USER_ID, xUserId);
    }
  }

  private boolean validateDeviceId(HttpServletResponse response, CachedBodyHttpServletRequest cachedBodyHttpServletRequest, 
      String phoneNumber) throws IOException {
    
    String deviceId = cachedBodyHttpServletRequest.getHeader("deviceId");
    if (StringUtils.isNotEmpty(deviceId)) {
      RiderLoginDeviceService riderLoginDeviceService = BeanUtilService.getBean(RiderLoginDeviceService.class);
      log.info("Inside rider search service , to validate and user");
      boolean isDeviceIdValid = riderLoginDeviceService.validateRiderDeviceId(phoneNumber, deviceId);
      if (!isDeviceIdValid) {
        setInvalidDeviceIdResponse(response);
        return false;
      }
      }
      log.info("Device Id Valid for phoneNumber:{}", phoneNumber);
    return true;
  }

  private boolean validatePathParamsRequest(HttpServletResponse response,
      CachedBodyHttpServletRequest cachedBodyHttpServletRequest, String phoneNumber) throws IOException {
    // Extract Path Variables
    String requestPath[] = cachedBodyHttpServletRequest.getRequestURI().split("/");

    List<String> requestPathList = Arrays.stream(requestPath)
        .filter(pathVariable -> !StringUtils.isAlpha(pathVariable)
            && (StringUtils.isAlphanumeric(pathVariable) || StringUtils.isNumeric(pathVariable)))
        .collect(Collectors.toList());

    if (requestPathList.size() >= 1) {
      String pathVariable = requestPathList.get(requestPathList.size() - 1);
      log.info("PathVariable -> " + pathVariable + "-" + StringUtils.isNumeric(pathVariable));
      if (StringUtils.isNumeric(pathVariable)) {
        log.info("Path param is a mobile number, validating the same");
        if (!phoneNumber.equals(pathVariable)) {
          log.info("Phone number of the rider doesn't match");
          setForbiddenResponse(response);
          return false;
        }
      } else if (StringUtils.isAlphanumeric(pathVariable) && !validateUserAuthorization(pathVariable,phoneNumber)) {
        setForbiddenResponse(response);
        return false;
      }
      cachedBodyHttpServletRequest.setAttribute(Constants.X_USER_ID, pathVariable);
      //Validate Device Id
      if(!cachedBodyHttpServletRequest.getRequestURI().contains("/login-device")) {
        return validateDeviceId(response, cachedBodyHttpServletRequest, phoneNumber);
      }
    }

    return true;
  }

  private boolean validateRequestBody(HttpServletResponse response, String body, String phoneNumber, CachedBodyHttpServletRequest request) throws IOException {
      String riderProfileId = "";
      ObjectMapper objectMapper = new ObjectMapper();
      log.info("Validating request body");
      JsonNode jsonNode = objectMapper.readTree(body);
      if(ObjectUtils.isNotEmpty(jsonNode.get("phoneNumber")) && !phoneNumber.equals(jsonNode.get("phoneNumber").asText())){
         log.info("Phone number change request {} to {}", phoneNumber, jsonNode.get("phoneNumber").asText());
         if(request.getRequestURI().contains("/profile") && request.getMethod().equals("POST")){
            setForbiddenResponse(response);
            return false;
         }
      }
      if (ObjectUtils.isNotEmpty(jsonNode.get("id")) && (ObjectUtils.isNotEmpty(jsonNode.get("riderProfileId")) || ObjectUtils.isNotEmpty(jsonNode.get("profileId"))) || ObjectUtils.isNotEmpty(jsonNode.get("riderId"))) {
        riderProfileId = getRiderId(jsonNode, riderProfileId);
      }else if(ObjectUtils.isNotEmpty(jsonNode.get("id"))){
        riderProfileId = jsonNode.get("id").asText();
      }else {
        riderProfileId = getRiderId(jsonNode, riderProfileId);
      }

    log.info("rider id is {}", riderProfileId);

    if (!validateUserAuthorization(riderProfileId, phoneNumber)) {
      setForbiddenResponse(response);
      return false;
    }
    if(StringUtils.isNotBlank(riderProfileId)) {
      request.setAttribute(Constants.X_USER_ID, riderProfileId);
    }

    //Validate Device Id
    if(!request.getRequestURI().contains("/login-device") && !StringUtils.isEmpty(riderProfileId)) {
      return validateDeviceId(response, request, riderProfileId);
    }
    return true;
  }

  private String getRiderId(JsonNode jsonNode, String riderProfileId) {
    if (ObjectUtils.isNotEmpty(jsonNode.get("riderProfileId"))) {
      riderProfileId = jsonNode.get("riderProfileId").asText();
    } else if (ObjectUtils.isNotEmpty(jsonNode.get("profileId"))) {
      riderProfileId = jsonNode.get("profileId").asText();
    } else if (ObjectUtils.isNotEmpty(jsonNode.get("riderId"))) {
      riderProfileId = jsonNode.get("riderId").asText();
    }
    return riderProfileId;
  }

  private String getPhoneNumberUsingToken(String idToken) throws AuthenticationException {
    TokenUtils tokenUtils = BeanUtilService.getBean(TokenUtils.class);
    return tokenUtils.getPhoneNumberFromIdToken(idToken);
  }

  private boolean validateAccessIdToken(String idToken, String accessToken){
    TokenUtils tokenUtils = BeanUtilService.getBean(TokenUtils.class);
    return tokenUtils.validateAccessIdToken(idToken, accessToken);
  }

  private boolean isOpsMember(String accessToken) throws AuthenticationException{
    TokenUtils tokenUtils = BeanUtilService.getBean(TokenUtils.class);
    boolean isOpsMember = tokenUtils.checkIfOpsToken(accessToken);
    log.info("The access belongs to Ops Member {}", isOpsMember);
    return isOpsMember;
  }

  boolean validateUserAuthorization(String riderProfileId, String phoneNumber) {
    log.info("Going to validate rider {} ", riderProfileId);
    if(StringUtils.isEmpty(riderProfileId)){
      log.info("The rider doesn't exist in request body. Skipping validation in request body");
      return true;
    }
    RiderSearchService riderSearchService = BeanUtilService.getBean(RiderSearchService.class);
    log.info("Inside rider search service , to validate and user");
    Optional<RiderProfile> riderProfile = riderSearchService
                                             .findRiderProfileByRiderIdAndPhoneNumber(riderProfileId, phoneNumber);
    riderProfile.ifPresent(riderDocument -> publishRiderAvialibilityKafkaMessage(riderDocument));
    
    return riderProfile.isPresent();
  }
  
  private void publishRiderAvialibilityKafkaMessage(RiderProfile riderProfile) {
    //Publish Kafka Message to Track Genuine Active Riders on Graphana.
    RiderAvailabilityKafkaPublisher riderAvailabilityKafkaPublisher = BeanUtilService.getBean(RiderAvailabilityKafkaPublisher.class);
    riderAvailabilityKafkaPublisher.publish(riderProfile);
  }


  private void setForbiddenResponse(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 401.
    response.setHeader("message", "User is not authorized to perform this operation");
  }
  
  private void setInvalidDeviceIdResponse(HttpServletResponse response) throws IOException {
    response.setHeader("message", "User device id is invalid");
    response.setStatus(437);
    ApiError apiError = new ApiError("437", DEVICE_ID_INVALID_LOGOUT, "User device id is invalid please logout");
    response.getWriter().write(new ObjectMapper().writeValueAsString(apiError));
  }
}
