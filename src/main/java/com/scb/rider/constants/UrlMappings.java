package com.scb.rider.constants;

public interface UrlMappings {
    String CURIE_NAMESPACE = "rider";

     String SLASH = "/";

     String DOCS_DIR = SLASH + "docs";

     String API = SLASH + "api";

     String VERSION_v1 = "v1";

     String VERSION_v2 = "v2";

     String CURRENT_VERSION = VERSION_v1;

     String RIDER_API = SLASH + "profile";
    
     String RIDER_DASHBOARD_API = "/rider/dashboard/";

     String PATH_VARIABLE_ID = "{id}";

     String PATH_VARIABLE_RIDER_ID = "{riderId}";

    public interface FileHandler {
        // ------------------------ URIs ------------------------------------
         String UPLOAD_FILE = SLASH + PATH_VARIABLE_ID + SLASH +"upload";
         String MULTI_UPLOAD_FILE = SLASH + PATH_VARIABLE_ID + SLASH +"multi-upload";

         String DOWNLOAD_FILE = SLASH + PATH_VARIABLE_ID + SLASH +"download";

         String COVID_SELFIE = SLASH + PATH_VARIABLE_ID + SLASH +"covid-selfie";

         String DOWNLOAD_COVID_SELFIE = COVID_SELFIE+ SLASH +"download";

    }

    public interface RiderJobDetailsUri {
        // ------------------------ URIs ------------------------------------
         String RIDER_JOB = SLASH + PATH_VARIABLE_ID + SLASH +"job";

         String RIDER_RECONCILIATION = SLASH + "reconciliation" ;

    }

    public interface RiderVehicleRegistration{
           String RIDER_VEHICLE_REGISTRATION = SLASH + PATH_VARIABLE_ID + SLASH + "vehicle-details";
          String RIDER_FOODCARD_DETAILS = SLASH + PATH_VARIABLE_ID + SLASH + "foodcard-details";
          String RIDER_VEHICLE_STATUS_UPDATE = SLASH + PATH_VARIABLE_ID + SLASH + "rider-vehicle-status";
          String RIDER_FOODCARD_SIZE_UPDATE = RIDER_VEHICLE_REGISTRATION + SLASH + "foodcart-size";
    }

    public interface RiderDrivingLicense {


        String RIDER_DRIVING_LICENSE = SLASH + PATH_VARIABLE_ID + SLASH + "license-details";

        String RIDER_DRIVING_LICENSE_BY_ID = SLASH + "license-details" + SLASH + PATH_VARIABLE_ID;

    }
    
    public interface RiderBackgroundVerification {
        String RIDER_BACKGROUND_VERIFICATION = SLASH + PATH_VARIABLE_ID + SLASH + "background-details";
        String RIDER_BACKGROUND_VERIFICATION_BY_RIDER = SLASH + PATH_VARIABLE_ID + SLASH + "background-details/rider";
    }

    public interface RiderZone{

        String ZONE = SLASH+"preferred-zone";
        String ZONE_OPS = SLASH+"preferred-zone/ops";
    }

    public interface RiderTraining {
    
    	 String TRAINING = RIDER_API + SLASH + "training";
    
    	 String STATUS = SLASH + "status";

    	 String APPOINTMENT = SLASH + "appointment";

		 String STATUS_UPDATE = SLASH + "update-status";

		 String ALL_AVAILABLE = SLASH + "all-available";
        
    }
    
    interface RiderEVForm {

    	String RIDER_EV_FORM = RIDER_API + SLASH +"ev-form";
    }
    
    public interface RiderManagementDashBoard {
      
       String SUMMARY = "summary";
      
    }
    
    public interface NewsNotifications {
      
      String NOTIFICATION_TRACKING = "/news-promotions/tracking/";
    }
}
