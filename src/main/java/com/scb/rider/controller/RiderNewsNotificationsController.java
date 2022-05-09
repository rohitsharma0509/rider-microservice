package com.scb.rider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scb.rider.client.NewsPromotionFeignClient;
import com.scb.rider.constants.UrlMappings.NewsNotifications;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RiderNewsNotificationsController {

  @Autowired
  private NewsPromotionFeignClient newsPromotionFeignClient;

  private static final String LOG_FORMAT = "RiderNewsNotificationsController-{}-{}";

  @PostMapping(value = NewsNotifications.NOTIFICATION_TRACKING + "/{riderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Object addRiderTrackingDetails(@PathVariable("riderId") String riderId,
      @RequestBody Object riderNotificationTracking) {

    log.info(LOG_FORMAT, "riderId:" + riderId,
        "riderNotificationTracking: " + riderNotificationTracking);
    return newsPromotionFeignClient.addRiderTrackingDetails(riderNotificationTracking);
  }
  
  
  @GetMapping(value = "/news-promotions/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Object getRiderNotificationDetails(
      @PathVariable(name = "riderId", required = true) String riderId,
      @RequestParam(name = "notificationType", required = true) String notificationType,
      @RequestParam(name = "offset", defaultValue = "0") int offset,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    log.info(LOG_FORMAT, "fetching Rider Notification details for riderId:", riderId);
    return newsPromotionFeignClient.getRiderNotificationDetails(riderId, notificationType, offset, size);
  }

  @GetMapping(value = "/unread-news-promotions/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Object getRiderUnreadNotificationDetails(
      @PathVariable(name = "riderId", required = true) String riderId) {
    log.info(LOG_FORMAT, "fetching Rider Unread Notification details for riderId:", riderId);
    return newsPromotionFeignClient.getRiderUnreadNotificationDetails(riderId);
  }
  

}
