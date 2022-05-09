package com.scb.rider.view;

public class View {
    private View(){}
    public static interface RiderJobDetailsView {
    }
    public static interface RiderDevice {
        // View for User POST call request body
        public static interface REQUEST {
        }
        // View for User POST call request body
        public static interface RESPONSE {
        }
        public static interface NotificationRequest{
        }
        public static interface NotificationResponse{
        }
    }
    
    
    public static interface RiderRemark {
        // View for User POST call request body
        public static interface REQUEST {
        }
        // View for User POST call request body
        public static interface RESPONSE {
        }
        
    }
}
