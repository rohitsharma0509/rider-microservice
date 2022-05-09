package com.scb.rider.model.document;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.view.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class RiderDeviceDetails {
    @Id
    @JsonView(value = {View.RiderDevice.RESPONSE.class})
    private String id;
    @JsonView(value = {View.RiderDevice.RESPONSE.class})
    @Indexed(unique = true)
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String profileId;
    @JsonView(value = {View.RiderDevice.REQUEST.class, View.RiderDevice.RESPONSE.class,
            View.RiderDevice.NotificationRequest.class})
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String deviceToken;
    @JsonView(value = {View.RiderDevice.REQUEST.class, View.RiderDevice.RESPONSE.class,
            View.RiderDevice.NotificationRequest.class})
    @NotNull(message = "{api.rider.profile.blank.msg}")
    private Platform platform;
    @JsonView(value = {View.RiderDevice.RESPONSE.class, View.RiderDevice.NotificationResponse.class})
    private String arn;
    
    @JsonView(value = {View.RiderDevice.REQUEST.class, View.RiderDevice.RESPONSE.class,
            View.RiderDevice.NotificationRequest.class})
    private String iosDeviceToken;
}
