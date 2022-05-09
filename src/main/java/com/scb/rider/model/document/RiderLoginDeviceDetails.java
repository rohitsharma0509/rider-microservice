package com.scb.rider.model.document;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.view.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@Document
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RiderLoginDeviceDetails {
    @Id
    @JsonView(value = {View.RiderDevice.RESPONSE.class})
    private String id;
    @JsonView(value = {View.RiderDevice.RESPONSE.class})
    @Indexed(unique = true)
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String phoneNumber;
    @JsonView(value = {View.RiderDevice.REQUEST.class, View.RiderDevice.RESPONSE.class,
            View.RiderDevice.NotificationRequest.class})
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String deviceId;
    
    private LocalDateTime creationDateTime;
    private LocalDateTime updationDateTime;
}
