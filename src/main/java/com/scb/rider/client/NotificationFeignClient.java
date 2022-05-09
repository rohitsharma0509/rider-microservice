package com.scb.rider.client;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.view.View;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationFeignClient", url = "${rider.client.notification-service}")
public interface NotificationFeignClient {

    @PostMapping(value = "/notification/endpoint", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(value = View.RiderDevice.NotificationResponse.class)
    public RiderDeviceDetails getDeviceArn(@JsonView(value = View.RiderDevice.NotificationRequest.class)
                                               @RequestBody  RiderDeviceDetails body);
}
