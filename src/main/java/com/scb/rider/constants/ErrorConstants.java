package com.scb.rider.constants;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
    public static final String FEIGN_EX_MSG = "api.rider.profile.feign.msg";


    private ErrorConstants() {
    }
    public static final String DOCUMENT_ALREADY_APPROVED_EXCEPTION = "api.rider.document.already.approved";
    public static final String TYPE_MISMATCH_EX_MSG = "api.rider.profile.typeMisMatch.msg";
    public static final String MISSING_PART_EX_MSG = "api.rider.profile.missingPart.msg";
    public static final String MISSING_PARAM_EX_MSG = "api.rider.profile.missingRequestParameter.msg";
    public static final String ARGUMENT_MISMATCH_EX_MSG = "api.rider.profile.argumentMismatch.msg";
    public static final String NO_HANDLER_EX_MSG = "api.rider.profile.noHandler.msg";
    public static final String NO_HTTP_METHOD_EX_MSG = "api.rider.profile.noHttpMethod.msg";
    public static final String MEDIA_NOT_SUPPORT_EX_MSG = "api.rider.profile.mediaNotSupport.msg";
    public static final String SERVER_ERROR_EX_MSG = "api.rider.profile.serverError.msg";
    public static final String ACCESS_DENIED_EX_MSG = "api.rider.profile.accessDenied.msg";
    public static final String DATA_NOT_FOUND_EX_MSG = "api.rider.profile.dataNotFound.msg";
    public static final String DUPLICATE_ATTR_MSG = "api.rider.profile.duplicateAttribute.msg";
    public static final String MANDATORY_FIELD_MISSING_EX_MSG = "api.rider.profile.null.msg";
    public static final String MANDATORY_CHECKS_MISSING_EX_MSG = "api.rider.profile.mandatoryChecksMissing.msg";
    public static final String STATUS_TRANSITION_NOT_ALLOWED_EX_MSG = "api.rider.profile.statusTransitionNotAllowed.msg";
    public static final String AWS_KEY_NOT_FOUND_EX_MSG = "api.rider.profile.key.not.found.msg";
    
    public static final String RIDER_DRIVING_LICENSE_NUMBER_ALREADY_EXIST = "api.rider.driving.license.number.already.exist";
    public static final String RIDER_VEHICLE_REG_NUMBER_ALREADY_EXIST = "api.rider.vehicle.reg.number.already.exist";
    public static final String RECORD_STATUS_UPDATED_SUCCESSFULLY = "record.status.updated.successfully";
    public static final String RECORD_STATUS_UPDATE_FAILED = "record.status.update.failed";
    public static final String RIDER_JOB_ALREADY_COMPLETED_ERROR = "rider.job.already.completed.error";
    
	public static final String SEAT_ALREADY_OCCUPIED_EXCEPTION = "rider.training.seat.already.occupied";
	public static final String TRAINING_ALREADY_COMPLETED_EXCEPTION = "rider.training.already.completed";
	public static final String APPOINTMENT_ID_NOT_FOUND = "rider.training.appointment.id.not.found";

	public static final String JOB_CANCELLED = "rider.job.cancelled.by.operator";
	public static final String FOOD_ALREADY_DELIVERED = "food.already.delivered";
    public static final String INVALID_STATE_TRANSITION = "invalid.job.state.transition";

	public static final String FEIGN_CLIENT_ERR = "api.rider.profile.feignclient.err.msg";
	public static final String  PAYLOAD_TOO_LARGE = "api.rider.payload.too.large.msg";
	public static final String PDPA_EXCEPTION = "api.rider.pdpa.exception";
	public static final String LONG_WAIT_PAID_MSG = "long.wait.paid.msg";

}
