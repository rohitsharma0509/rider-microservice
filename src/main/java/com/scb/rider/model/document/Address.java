package com.scb.rider.model.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private String landmark;
    private String city;
    private String country;
    private String village;
    private String district;
    private String state;
    private String countryCode;
    private String zipCode;
    private String floorNumber;
    private String unitNumber;
}
