package com.scb.rider.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.scb.rider.client.NewsPromotionFeignClient;

@ExtendWith(MockitoExtension.class)
public class RiderNewsNotificationsControllerTest {

  @InjectMocks
  private RiderNewsNotificationsController riderNewsNotificationsController;

  @Mock
  private NewsPromotionFeignClient newsPromotionFeignClient;

  @Test
  void addRiderTrackingDetailsTest() {
    riderNewsNotificationsController.addRiderTrackingDetails(anyString(), "");
    verify(newsPromotionFeignClient,times(1)).addRiderTrackingDetails("");
  }
  
  @Test
  void getRiderNotificationDetailsTest() {
    riderNewsNotificationsController.getRiderNotificationDetails(anyString(), any(), anyInt(), anyInt());
    verify(newsPromotionFeignClient,times(1)).getRiderNotificationDetails(anyString(), any(), anyInt(), anyInt());
  }

  @Test
  void getRiderUnreadNotificationDetailsTest() {
    riderNewsNotificationsController.getRiderUnreadNotificationDetails(anyString());
    verify(newsPromotionFeignClient,times(1)).getRiderUnreadNotificationDetails(anyString());
  }
}
