package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Document
public class RiderProfile extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  private String id;
  @Indexed(unique = true)
  private String riderId;
  @Indexed(sparse = true)
  private String firstName;
  @Indexed(sparse = true)
  private String lastName;
  private Address address;
  private NationalAddress nationalAddress;
  private String dob;
  private String gender;
  @Indexed(unique = true, sparse = true)
  private String nationalID;
  private MandatoryCheckStatus nationalIdStatus;
  @Indexed(unique = true, sparse = true)
  private String accountNumber;
  private String countryCode;
  @Indexed(unique = true)
  private String phoneNumber;
  @Indexed
  private RiderStatus status;
  private MandatoryCheckStatus profilePhotoStatus;
  private LocalDateTime latestStatusModifiedDate;
  private String reason;
  private String remarks;
  private LocalDateTime suspensionExpiryTime;
  private Integer suspensionDuration;
  private String profilePhotoRejectionReason;
  private String profilePhotoRejectionComment;
  private LocalDateTime profilePhotoRejectionTime;
  
  private NationalIdDetails nationalIdDetails;
  
  @Indexed
  private AvailabilityStatus availabilityStatus;
  private RiderProfileStage profileStage;
  private BackgroundVerificationAttemptStatus attemptBGVStatus;
  private String isReadyForAuthorization;
  private boolean consentAcceptFlag;
  private boolean dataSharedFlag;

  private String tierName;
  private int tierId;
  private String approvalDateTime;

  private RiderPreferredZones riderPreferredZones;
 
  private Boolean evBikeUser;
  private Boolean rentingToday;
  private String updatedBy;
  private EvBikeVendors evBikeVendor;
  
  private RiderDocumentUpload riderDocumentUpload;

  private Integer mannerScoreCurrent;
  private Integer mannerScoreInitial;
}
