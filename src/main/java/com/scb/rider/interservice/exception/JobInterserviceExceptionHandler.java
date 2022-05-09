package com.scb.rider.interservice.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.exception.ApiError;
import com.scb.rider.util.PropertyUtils;

import feign.FeignException;
import feign.RetryableException;
import lombok.extern.log4j.Log4j2;

@ControllerAdvice(basePackages = "com.scb.rider.interservice.controller")
@Log4j2
public class JobInterserviceExceptionHandler extends ResponseEntityExceptionHandler{

    @Autowired
    private PropertyUtils propertyUtils;

    @Autowired
    private ObjectMapper objectMapper;

	@ExceptionHandler({ FeignException.class, RetryableException.class })
	public ResponseEntity<Object> dataFetchException(final FeignException ex, final WebRequest request){
		JobServiceErrorResponse jobServiceError = null;
		try {
			jobServiceError = objectMapper.readValue(ex.contentUTF8(), JobServiceErrorResponse.class);
		} catch (JsonProcessingException e) {
			log.error("Error occured while parsing the error response, {}", ex);
			final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error occured while parsing error response from the service",
					propertyUtils.getProperty(ErrorConstants.FEIGN_EX_MSG));
			return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
		}
		log.info(" Exception occured in Job Service is : {} ", jobServiceError);
		final ApiError apiError = new ApiError(HttpStatus.valueOf(ex.status()), 
				jobServiceError.getErrorMessage(),
				propertyUtils.getProperty(ErrorConstants.FEIGN_EX_MSG));
		return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.valueOf(ex.status()));
		}
	
}
