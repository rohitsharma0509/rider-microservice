package com.scb.rider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.scb.rider.constants.UrlMappings.NewsNotifications;
import com.scb.rider.model.dto.RiderFireBaseNotificationDetailsDto;

@FeignClient(name = "newsPromotionFeignClient", url = "${rider.client.newsPromotion-service}")
public interface NewsPromotionFeignClient {

  @PutMapping(value = "/news-promotions/register-device/{riderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void registerDeviceToTopic(@PathVariable("riderId") String riderId,
      @RequestBody @Validated final RiderFireBaseNotificationDetailsDto riderDetailsDto);

  @PostMapping(value = NewsNotifications.NOTIFICATION_TRACKING)
  Object addRiderTrackingDetails(@RequestBody Object riderNotificationTracking);

  @GetMapping(value = "/news-promotions/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  Object getRiderNotificationDetails(@PathVariable(name = "riderId") String riderId,
      @RequestParam(name = "notificationType") String notificationType,
      @RequestParam(name = "offset") int offset, @RequestParam(name = "size") int size);
  
  @GetMapping(value = "/unread-news-promotions/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  Object getRiderUnreadNotificationDetails(@PathVariable(name = "riderId") String riderId);

}
