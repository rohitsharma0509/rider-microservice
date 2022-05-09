package com.scb.rider.service;



import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import com.scb.rider.model.document.RiderLoginDeviceDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderLoginDeviceDetailRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderLoginDeviceService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderLoginDeviceServiceTest {

  @Mock
  private RiderLoginDeviceDetailRepository riderLoginDeviceDetailRepository;
  @Mock
  private RiderProfileRepository riderProfileRepository;
  @InjectMocks
  private RiderLoginDeviceService riderLoginDeviceService;

  private String riderId = "1234";

  @Test
  public void saveRiderLoginDeviceInfoTest() {

    RiderLoginDeviceDetails request =
        RiderLoginDeviceDetails.builder().deviceId("abc").phoneNumber("1234").build();
    RiderLoginDeviceDetails riderResponse =
        RiderLoginDeviceDetails.builder().deviceId("abc").id("12344").build();
    RiderProfile riderProfile = new RiderProfile();
    riderProfile.setId("1234");
    riderProfile.setPhoneNumber("1231313");

    when(riderProfileRepository.findById(riderId)).thenReturn(Optional.of(riderProfile));
    when(riderLoginDeviceDetailRepository.findByPhoneNumber(riderId))
        .thenReturn(Optional.of(riderResponse));

    when(riderLoginDeviceDetailRepository.save(riderResponse)).thenReturn(riderResponse);
    RiderLoginDeviceDetails result =
        riderLoginDeviceService.saveRiderLoginDeviceInfo(riderId, request);

    assertTrue(ObjectUtils.isNotEmpty(result));
  }

  @Test
  public void saveRiderLoginDeviceInfoWithNewRecordTest() {

    RiderLoginDeviceDetails request =
        RiderLoginDeviceDetails.builder().deviceId("abc").phoneNumber("1234").build();
    RiderLoginDeviceDetails riderResponse =
        RiderLoginDeviceDetails.builder().deviceId("abc").id("12344").build();
    RiderProfile riderProfile = new RiderProfile();
    riderProfile.setId("1234");
    riderProfile.setPhoneNumber("1231313");

    when(riderProfileRepository.findById(riderId)).thenReturn(Optional.of(riderProfile));
    when(riderLoginDeviceDetailRepository.findByPhoneNumber(riderId)).thenReturn(Optional.empty());

    when(riderLoginDeviceDetailRepository.save(riderResponse)).thenReturn(riderResponse);
    RiderLoginDeviceDetails result =
        riderLoginDeviceService.saveRiderLoginDeviceInfo(riderId, request);

    assertFalse(ObjectUtils.isNotEmpty(result));
  }

  @Test
  public void validateRiderDeviceIdTest() {
    RiderLoginDeviceDetails request =
        RiderLoginDeviceDetails.builder().deviceId("abc").phoneNumber("1234").build();
    RiderLoginDeviceDetails riderResponse =
        RiderLoginDeviceDetails.builder().deviceId("abc").id("12344").build();

    when(riderLoginDeviceDetailRepository.findByPhoneNumber(riderId))
        .thenReturn(Optional.of(riderResponse));

    boolean isValid = riderLoginDeviceService.validateRiderDeviceId("123", "abc");
    assertEquals(isValid, true);
  }

  @Test
  public void validateRiderWrongDeviceIdTest() {
    RiderLoginDeviceDetails request =
        RiderLoginDeviceDetails.builder().deviceId("abc").phoneNumber("1234").build();
    RiderLoginDeviceDetails riderResponse =
        RiderLoginDeviceDetails.builder().deviceId("abc").id("12344").build();

    when(riderLoginDeviceDetailRepository.findByPhoneNumber(anyString()))
        .thenReturn(Optional.of(riderResponse));

    boolean isValid = riderLoginDeviceService.validateRiderDeviceId("123", "abcc");
    assertEquals(isValid, false);
  }
}
