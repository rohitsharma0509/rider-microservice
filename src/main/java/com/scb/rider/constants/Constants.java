package com.scb.rider.constants;

public final class Constants {

    private Constants(){}
    // TODO Refactor this code with enum
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String TWO = "2";
    public static final String X_INFO = "X-info";

    //Job Constants
    public static final String RIDER_JOB_STATUS_FAILED_CODE = "100";
    public static final String RIDER_JOB_STATUS_SUCCESS_CODE = "200";
    public static final String CREDENTIAL_ERROR_CODE = "101";
    public static final int APP_UPGRADE_REQUIRED = 435;
    public static final String FORCED_APP_UPGRADE_REQUIRED = "FORCED_APP_UPGRADE_REQUIRED";

    public static final String RIDER_JOB_STATUS_SUCCESS_MESSAGE = "Success";

    public static final String PRIORITY = "high";

    public static final String TITLE = "Job Cancelled";

    public static final String SOUND = "default";

    public static final String CLICK_ACTION = "MainActivity";

    public static final String JOB_CANCELLED = "JOB_CANCELLED";

    public static final String LONG_WAIT_PAYMENT = "LONG_WAIT_PAYMENT";
    public static final String LONG_WAIT_PAYMENT_MSG = "Long wait time payment";
    public static final String THRESHOLD_WAIT_TIME = "thresholdWaitTime";
    public static final String EXCESSIVE_WAIT_TOPUP_AMOUNT = "excessiveWaitTopupAmount";
    public static final String MANNER_SCORE_INITIAL = "mannerScoreInitial";
    public static final String MANNER_SCORE_POSSIBLE_MAX = "mannerScorePossibleMax";

    public static final String TYPE = "JOB_CANCELLED";
    
    //Date Format
    public static final String DATE_FORMAT = "";

    public static final String LOG_FORMAT_CONTROLLER_START = "Controller {} for method {} started for RequestId-{} [{}:{}]";
    public static final String LOG_FORMAT_CONTROLLER_END = "Controller {} for method {} ended for RequestId-{}";
    public static final String LOG_FORMAT_SERVICE_START = "Service {} for method {} started for RequestId-{} [{}:{}]";
    public static final String LOG_FORMAT_SERVICE_END = "Service {} for method {} ended for RequestId-{}";
    public static final String LOG_FORMAT_REPO_START = "Repository {} for method {} started for RequestId-{} [{}:{}]";
    public static final String LOG_FORMAT_REPO_END = "Repository {} for method {} ended for RequestId-{}";
    public static final String LOG_FORMAT_FEIGN_CLIENT_START = "FeignClient {} for method {} started for RequestId-{} [{}:{}]";
    public static final String LOG_FORMAT_FEIGN_CLIENT_END = "FeignClient {} for method {} ended for RequestId-{}";
    public static final String LOG_FORMAT_EXCEPTION = "Exception thrown in class {} in method {} for RequestId-{}";
    public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    public static final String THAI_COUNTRY_CODE = "+66";
    public static final String IN_COUNTRY_CODE = "+91";
    public static final String THAI = "th";
    public static final String OPS = "Ops";
    public static final String RIDER = "Rider";

    public static final String BROADCASTING = "BROADCASTING";
    public static final String OTHER = "other";
    public static final String OTHER_IN_THAI = "อื่นๆ";

    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final int CODE_100 = 100;
    public static final int ERROR_CODE_101 = 101;
    public static final int ERROR_CODE_102 = 102;
    public static final int ERROR_CODE_103 = 103;
    public static final String RIDER_ID = "riderId";
    public static final String NATIONAL_ID = "nationalId";
    public static final String PHONE_NUMBER = "phoneNumber";

    public static final String THAI_NATIONAL_ID_CODE = "P1";
    public static final String CROSS_SELL_CONSENT_CODE = "002";
    public static final String MARKETING_CONSENT_CODE = "001";
    public static final String RIDER_CHANNEL_CODE = "002";
    public static final String X_USER_ID = "X-User-Id";
    public static final String OPS_MEMBER = "OPS-Member";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final Long RIDER_TOKEN_REDIS_CACHE_TTL = 86400L;
    public static final String BKK_ZONE_ID = "Asia/Bangkok";
    public static final String RIDER_ON_ACTIVE_JOB = "riderOnActiveJob";
    public static final String RIDER_ON_TRAINING_TODAY = "riderOnTrainingToday";
    public static final String ALL_RIDERS = "allRiders";
    public static final int PAGE_SIZE_10K = 10000;
    public static final String UNAUTHORIZED_TH = "\u0E23\u0E2D\u0E2D\u0E19\u0E38\u0E21\u0E31\u0E15\u0E34";
    public static final String AUTHORIZED_TH = "\u0E2D\u0E19\u0E38\u0E21\u0E31\u0E15\u0E34";
    public static final String SUSPENDED_TH = "\u0E16\u0E39\u0E01\u0E23\u0E30\u0E07\u0E31\u0E1A";
    public static final String RIDER_ID_TH = "\u0e23\u0e2b\u0e31\u0e2a\u0e44\u0e23\u0e40\u0e14\u0e2d\u0e23\u0e4c";
    public static final String RIDER_NAME_TH = "\u0e0a\u0e37\u0e48\u0e2d\u0e44\u0e23\u0e40\u0e14\u0e2d\u0e23\u0e4c";
    public static final String PHONE_TH = "\u0e2b\u0e21\u0e32\u0e22\u0e40\u0e25\u0e02\u0e42\u0e17\u0e23\u0e28\u0e31\u0e1e\u0e17\u0e4c\u0e02\u0e2d\u0e07\u0e44\u0e23\u0e40\u0e14\u0e2d\u0e23\u0e4c";
    public static final String TIER_NAME_TH = "\u0E0A\u0E37\u0E48\u0E2D\u0E0A\u0E31\u0E49\u0E19";
    public static final String STATUS_TH = "\u0E2A\u0E16\u0E32\u0E19\u0E30";
    public static final String IS_READY_TH = "\u0E04\u0E27\u0E32\u0E21\u0E1E\u0E23\u0E49\u0E2D\u0E21\u0E2A\u0E33\u0E2B\u0E23\u0E31\u0E1A\u0E01\u0E32\u0E23\u0E15\u0E23\u0E27\u0E08\u0E2A\u0E2D\u0E1A";
    public static final String APPROVAL_DATE_TH = "\u0E27\u0E31\u0E19\u0E17\u0E35\u0E48\u0E2D\u0E19\u0E38\u0E21\u0E31\u0E15\u0E34";
    public static final String WORK_AREA_TH = "\u0E40\u0E02\u0E15\u0E17\u0E33\u0E07\u0E32\u0E19";
    public static final String REGISTRATION_DATE_TH = "\u0E27\u0E31\u0E19\u0E17\u0E35\u0E48\u0E25\u0E07\u0E17\u0E30\u0E40\u0E1A\u0E35\u0E22\u0E19";
    public static final String READY_TH = "\u0E1E\u0E23\u0E49\u0E2D\u0E21";
    public static final String NOT_READY_TH = "\u0E22\u0E31\u0E07\u0E44\u0E21\u0E48\u0E1E\u0E23\u0E49\u0E2D\u0E21";
    public static final String DATE_WITH_SHORT_MONTH_NAME = "dd MMM yyyy";
    public static final String DATETIME_FULL ="yyyy-MM-dd'T'HH:mm:ss.SSSX";
}
