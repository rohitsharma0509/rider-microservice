package com.scb.rider.model.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document
public class NationalAddress implements Serializable {
    private static final long serialVersionUID = 1L;
    private String buildingName;
    private String roomNumber;
    private String floor;
    private String number;
    private String alley;
    private String neighbourhood;
    private String road;
    private String subdistrict;
    private String district;
    private String province;
    private String postalCode;
}