package com.scb.rider.exception;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.util.PropertyUtils;

@ControllerAdvice
public class MaxFileSizeExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private PropertyUtils propertyUtils;

	private static final String ERROR = "error";

	@Value("${upload.maxFileSizeMB}")
    private long maxFileUploadSize;

	@ExceptionHandler({ MaxUploadSizeExceededException.class })
	public ResponseEntity<Object> handleMaxUploadSizeExceededException(final Exception ex, final WebRequest request) {
		logger.info(ex.getClass().getName());
		logger.error(ERROR, ex);
		String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.PAYLOAD_TOO_LARGE),
				maxFileUploadSize);
		final ApiError apiError = new ApiError(HttpStatus.PAYLOAD_TOO_LARGE.name(), ex.getMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.PAYLOAD_TOO_LARGE);
	}

}
