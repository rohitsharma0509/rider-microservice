package com.scb.rider.exception;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.scb.rider.constants.Constants;
import io.swagger.annotations.Api;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.util.PropertyUtils;

import feign.FeignException;
import feign.RetryableException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice(basePackages = "com.scb.rider.controller")
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private PropertyUtils propertyUtils;

    private static final String ERROR = "error";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Method Argument not valid", errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Bind exception", errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.TYPE_MISMATCH_EX_MSG), ex.getValue(), ex.getPropertyName(), ex.getRequiredType());
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "TypeMisMatch exception", error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.MISSING_PART_EX_MSG), ex.getRequestPartName());
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Missing servlet requestPart", error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.MISSING_PARAM_EX_MSG), ex.getParameterName());
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Missing servlet request parameter", error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.ARGUMENT_MISMATCH_EX_MSG), ex.getName());
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Method argument type mismath", error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.NO_HANDLER_EX_MSG), ex.getHttpMethod(), ex.getRequestURL());
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "No handler exception", error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" " + propertyUtils.getProperty(ErrorConstants.NO_HTTP_METHOD_EX_MSG) + " ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, "Request method not supported", builder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" " + propertyUtils.getProperty(ErrorConstants.MEDIA_NOT_SUPPORT_EX_MSG) + " ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));
        logger.error("Exception: "+ex.getLocalizedMessage());
        final ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media type not supported", builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<Object> handleDataNotFoundException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.DATA_NOT_FOUND_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MandatoryFieldMissingException.class})
    public ResponseEntity<Object> handleMandatoryFieldMissingException(final MandatoryFieldMissingException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.MANDATORY_FIELD_MISSING_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({MandatoryChecksMissingException.class})
    public ResponseEntity<Object> handleMandatoryChecksMissingException(final MandatoryChecksMissingException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.MANDATORY_CHECKS_MISSING_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({StatusTransitionNotAllowedException.class})
    public ResponseEntity<Object> handleStatusTransitionNotAllowedException(final StatusTransitionNotAllowedException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.STATUS_TRANSITION_NOT_ALLOWED_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({SeatAlreadyOccupiedException.class})
    public ResponseEntity<Object> handleSeatAlreadyOccupiedException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.SEAT_ALREADY_OCCUPIED_EXCEPTION));
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({TrainingAlreadyCompletedException.class})
    public ResponseEntity<Object> handleTrainingAlreadyCompletedException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.TRAINING_ALREADY_COMPLETED_EXCEPTION));
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DocumentAlreadyApprovedException.class})
    public ResponseEntity<Object> handleDocumentAlreadyApprovedException(final DocumentAlreadyApprovedException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(),
                propertyUtils.getProperty(ErrorConstants.DOCUMENT_ALREADY_APPROVED_EXCEPTION,ex.getObjects()));
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AppointmentIdNotFoundException.class})
    public ResponseEntity<Object> handleAppointmentIdNotFoundException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.APPOINTMENT_ID_NOT_FOUND));
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({JobNotAcceptedException.class,JobTimeOutException.class})
    public ResponseEntity<Object> handleJobNotAcceptedException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({ RiderAlreadyExistsException.class })
    public ResponseEntity<Object> handleRiderAlreadyExistsException(final RiderAlreadyExistsException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({JobAlreadyAcceptedException.class})
    public ResponseEntity<Object> handleJobAlreadyAcceptedException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError("JOB_ALREADY_ACCEPTED", ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidImageExtensionException.class})
    public ResponseEntity<Object> handleInvalidImageExtensionExceptionException(final InvalidImageExtensionException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({DuplicateKeyException.class})
    public ResponseEntity<Object> mongoDbException(final DuplicateKeyException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.DUPLICATE_ATTR_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Exception occured", propertyUtils.getProperty(ErrorConstants.SERVER_ERROR_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final Exception ex, final WebRequest request) {
        logger.info("request" + request.getUserPrincipal());
        return new ResponseEntity<>(propertyUtils.getProperty(ErrorConstants.ACCESS_DENIED_EX_MSG), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({RiderJobStatusValidationException.class})
    public ResponseEntity<Object> handleRiderJobStatusValidationException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
    
    @ExceptionHandler({InvalidInputException.class})
    public ResponseEntity<Object> handleInvalidInputException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
    
    @ExceptionHandler({AmazonS3Exception.class})
    public ResponseEntity<Object> handleKeyNotFound(final Exception ex, final WebRequest request) {
      logger.info(ex.getClass().getName());
      logger.error(ERROR, ex);
      final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND,
          propertyUtils.getProperty(ErrorConstants.AWS_KEY_NOT_FOUND_EX_MSG),
          propertyUtils.getProperty(ErrorConstants.AWS_KEY_NOT_FOUND_EX_MSG));
      return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({JobNotFoundException.class})
    public ResponseEntity<Object> handleJobNotFoundException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(),ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({TrainingSlotEmptyException.class})
    public ResponseEntity<Object> handleTrainingSlotEmptyException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.OK, ex.getMessage(),ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ FeignException.class , RetryableException.class})
    public ResponseEntity<Object> dataFetchException(final FeignException ex, final WebRequest request){
        logger.info(ex.getClass().getName());
        logger.error("error", ex);
        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error occured while an internal call",
                propertyUtils.getProperty(ErrorConstants.FEIGN_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
    
    @ExceptionHandler({ PdpaException.class })
    public ResponseEntity<Object> pdpaConectionException(final PdpaException ex, final WebRequest request){
        logger.info(ex.getClass().getName());
        logger.error("error", ex);
        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                propertyUtils.getProperty(ErrorConstants.PDPA_EXCEPTION));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
    
    @ExceptionHandler({JobCancelledByOperatorException.class})
    public ResponseEntity<Object> handleJobCancelledByOperatorException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError("JOB_ALREADY_CANCELLED", ex.getMessage(),
        		propertyUtils.getProperty(ErrorConstants.JOB_CANCELLED));
        return new ResponseEntity<>(apiError, new HttpHeaders(),  HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({JobAlreadyCancelledException.class})
    public ResponseEntity<Object> handleJobAlreadyCancelledException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.EXPECTATION_FAILED, ex.getMessage(),      
        		propertyUtils.getProperty(ErrorConstants.JOB_CANCELLED));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({FoodAlreadyDeliveredException.class})
    public ResponseEntity<Object> handleFoodAlreadyDeliveredException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.EXPECTATION_FAILED, ex.getMessage(),
                propertyUtils.getProperty(ErrorConstants.FOOD_ALREADY_DELIVERED));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({InvalidStateTransitionException.class})
    public ResponseEntity<Object> handleInvalidStateTransitionException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.EXPECTATION_FAILED, ex.getMessage(),
                propertyUtils.getProperty(ErrorConstants.INVALID_STATE_TRANSITION));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({AppUpgradeRequiredException.class})
    public ResponseEntity<Object> handleAppUpgradeRequiredException(final AppUpgradeRequiredException ex) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(String.valueOf(Constants.APP_UPGRADE_REQUIRED), ex.getMessage(), Constants.FORCED_APP_UPGRADE_REQUIRED);
        return ResponseEntity.status(Constants.APP_UPGRADE_REQUIRED).body(apiError);
    }
    @ExceptionHandler(UpdatePhoneNumberException.class)
    public ResponseEntity<ApiError> handlePeakHourPricingException(UpdatePhoneNumberException ex){
        ApiError error = new ApiError();
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<ApiError>(error, BAD_REQUEST);
    }

    @ExceptionHandler({RiderMannerScoreException.class})
    public ResponseEntity<Object> handleRiderMannerScoreException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}