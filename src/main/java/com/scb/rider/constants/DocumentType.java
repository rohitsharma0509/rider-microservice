package com.scb.rider.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum DocumentType {
    PROFILE_PHOTO,
    DRIVER_LICENSE,
    VEHICLE_REGISTRATION,
    VEHICLE_WITH_FOOD_CARD,
    BACKGROUND_VERIFICATION_FORM,
    EV_FORM,
    FOOD_DELIVERED_PHOTO,
    MEAL_PICKUP_PHOTO,
    PARKING_RECIEPT;
  
  public static List<String> getPublicDocumentTypeList(){
    List<String> publicDocumentTypesList = new ArrayList<>();
    publicDocumentTypesList.add(DocumentType.PROFILE_PHOTO.name());
    return publicDocumentTypesList;
  }
  
  public static List<DocumentType> getMandatoryDocumentTypeList(){
    return Arrays.asList(PROFILE_PHOTO, DRIVER_LICENSE, VEHICLE_REGISTRATION, 
        VEHICLE_WITH_FOOD_CARD, BACKGROUND_VERIFICATION_FORM,EV_FORM);
  }
}
