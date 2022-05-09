package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@Document
public class RiderEmergencyContact extends BaseEntity {
    @Id
    private String id;
    @Indexed(name="profileId", direction = IndexDirection.ASCENDING, unique = true)
    private String profileId;
    private String name;
    private String mobilePhoneNumber;
    private String homePhoneNumber;
    private String relationship;
    private String address1;
    private String address2;
    private String district;
    private String subDistrict;
    private String province;
    private String zipCode;
}
