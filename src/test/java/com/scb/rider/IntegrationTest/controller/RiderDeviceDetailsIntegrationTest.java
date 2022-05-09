package com.scb.rider.IntegrationTest.controller;

import com.scb.rider.client.NewsPromotionFeignClient;
import com.scb.rider.client.NotificationFeignClient;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.Platform;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class RiderDeviceDetailsIntegrationTest extends AbstractRestApiIntegrationTest {

    static final String URL = "/profile/";
    @MockBean
    private NotificationFeignClient notificationFeignClient;

    @MockBean
    private NewsPromotionFeignClient newsPromotionFeignClient;

    @Test
    public void testCreateRiderDrivingLicenseRequest() throws Exception {

        RiderProfile riderProfile = createRiderProfileDb();
        // prepare data and mock's behaviour
        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("fK7q917CR0qNT4URN4CzGC:APA91bENpabTbmHd_-VFzigzSjE_KFGjVnIy4vCUDgAAdQrhqe9JfaZuYiEUUwGE8N-tOR9FmOz98qMDEBGQ_cNIMiAUf1M96hHUD3cjNFg8aStHut6ohRADVnBreljFPL1YUtM-AMz1")
                .platform(Platform.GCM)
                .build();

        RiderDeviceDetails notificationResponse = RiderDeviceDetails.builder()
                .arn("arn")
                .deviceToken("fK7q917CR0qNT4URN4CzGC:APA91bENpabTbmHd_-VFzigzSjE_KFGjVnIy4vCUDgAAdQrhqe9JfaZuYiEUUwGE8N-tOR9FmOz98qMDEBGQ_cNIMiAUf1M96hHUD3cjNFg8aStHut6ohRADVnBreljFPL1YUtM-AMz1")
                .build();

        String json = objectMapper.writeValueAsString(request);
        when(notificationFeignClient.getDeviceArn(any())).thenReturn(notificationResponse);
        doNothing().when(newsPromotionFeignClient).registerDeviceToTopic(anyString(), any());
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + riderProfile.getId() + "/device")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

        // verify that service method was called once

        RiderDeviceDetails riderDeviceDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDeviceDetails.class);
        assertNotNull(riderDeviceDetails);
        assertNotNull(riderDeviceDetails.getId());
        assertEquals(riderProfile.getId(), riderDeviceDetails.getProfileId(), "Invalid Profile Id");
    }
    
    @Test
    public void testCreateRiderDrivingLicenseRequestNotFoundRider() throws Exception {

        // prepare data and mock's behaviour
        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("fK7q917CR0qNT4URN4CzGC:APA91bENpabTbmHd_-VFzigzSjE_KFGjVnIy4vCUDgAAdQrhqe9JfaZuYiEUUwGE8N-tOR9FmOz98qMDEBGQ_cNIMiAUf1M96hHUD3cjNFg8aStHut6ohRADVnBreljFPL1YUtM-AMz1")
                .platform(Platform.GCM)
                .build();

        RiderDeviceDetails notificationResponse = RiderDeviceDetails.builder()
                .arn("arn")
                .deviceToken("fK7q917CR0qNT4URN4CzGC:APA91bENpabTbmHd_-VFzigzSjE_KFGjVnIy4vCUDgAAdQrhqe9JfaZuYiEUUwGE8N-tOR9FmOz98qMDEBGQ_cNIMiAUf1M96hHUD3cjNFg8aStHut6ohRADVnBreljFPL1YUtM-AMz1")
                .build();

        String json = objectMapper.writeValueAsString(request);
        when(notificationFeignClient.getDeviceArn(any())).thenReturn(notificationResponse);
        doNothing().when(newsPromotionFeignClient).registerDeviceToTopic(anyString(), any());

        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + "riderProfile" + "/device")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");

    }


    @Test
    public void testGetRiderDeviceResponseByProfileIdSuccess() throws Exception {
        RiderDeviceDetails riderDeviceDetail = createRiderDeviceInfoInDb();
        // execute
        MvcResult result = mockMvc
                .perform(
                        get(URL + riderDeviceDetail.getProfileId() + "/device")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderDeviceDetails riderDeviceDetails = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDeviceDetails.class);

        assertNotNull(riderDeviceDetails, "Rider Device Details is not found");

    }


    @Test
    public void testGetRiderDeviceDetailsByProfileIdNotFound() throws Exception {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = createRiderDrivingLicenseDocumentDb();
        // execute
        MvcResult result = mockMvc
                .perform(
                        get(URL + "/id-2" + "/device")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");

    }
}
