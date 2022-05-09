package com.scb.rider.service;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.model.document.RiderActiveStatusDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.ZoneEntity;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.repository.RiderActiveStatusDetailsRepository;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderInactiveTrackingServiceTest {

  @Mock
  private RiderActiveStatusDetailsRepository riderInactiveTrackingRepository;

  @InjectMocks
  private RiderActiveTrackingZoneService riderInactiveTrackingService;

  @Mock
	private LocationServiceFeignClient locationServiceFeignClient;
  
  private String riderId = "1234";

  private static RiderProfile riderProfile;

  private final static String userId = "6033912d5fc9421cd4b0d71a";

  @Test
  public void saveOrUpdateRiderInactiveStatusTest() {

    riderProfile = new RiderProfile();
    riderProfile.setAvailabilityStatus(AvailabilityStatus.JobInProgress);
    riderProfile.setId(userId);
    riderProfile.setRiderId(riderId);

    when(riderInactiveTrackingRepository.findByRiderId(anyString())).thenReturn(Optional.empty());
    riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
  }
  
  @Test
  public void addZoneDetails() {

    riderProfile = new RiderProfile();
    riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
    riderProfile.setId(userId);
    riderProfile.setRiderId(riderId);

    RiderActiveStatusDetails det=RiderActiveStatusDetails.builder().activeTime(LocalDateTime.now()).build();
    Optional<RiderActiveStatusDetails> op=Optional.of(det);
    ZoneEntity zoneEntity =ZoneEntity.builder().zoneName("asd").zoneId(1).build();
    when(locationServiceFeignClient.getRiderActiveZone(anyString())).thenReturn(zoneEntity);
    when(riderInactiveTrackingRepository.findByRiderId(anyString())).thenReturn(Optional.of(det));
    when(riderInactiveTrackingRepository.save(det)).thenReturn(det);
    riderInactiveTrackingService.addZoneDetails(riderProfile);
  }
  
  @Test
  public void addZoneDetailsWithoutZoneInfo() {

    riderProfile = new RiderProfile();
    riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
    riderProfile.setId(userId);
    riderProfile.setRiderId(riderId);

    RiderActiveStatusDetails det=RiderActiveStatusDetails.builder().activeTime(LocalDateTime.now()).build();
    Optional<RiderActiveStatusDetails> op=Optional.of(det);
    ZoneEntity zoneEntity =ZoneEntity.builder().zoneName("asd").zoneId(1).build();
    when(locationServiceFeignClient.getRiderActiveZone(anyString()))
    .thenThrow(new NullPointerException());
    when(riderInactiveTrackingRepository.findByRiderId(anyString())).thenReturn(Optional.of(det));
    when(riderInactiveTrackingRepository.save(det)).thenReturn(det);
    riderInactiveTrackingService.addZoneDetails(riderProfile);
  }

  @ParameterizedTest(name = "{index} => riderProfileStatus={0}, riderInactivityStatus={1}")
  @MethodSource("availabilityStatusProvider")
  public void saveOrUpdateRiderInactiveStatusWithRiderDocumentExistsWithJobInProgressTest(
      AvailabilityStatus riderProfileStatus, AvailabilityStatus riderInactivityStatus) {

    riderProfile = new RiderProfile();
    riderProfile.setAvailabilityStatus(riderProfileStatus);
    riderProfile.setId(userId);
    riderProfile.setRiderId(riderId);

    RiderActiveStatusDetails riderInactiveTrackingDetails = RiderActiveStatusDetails
        .builder().availabilityStatus(riderInactivityStatus).id(new ObjectId(userId)).riderId(riderId).build();

    when(riderInactiveTrackingRepository.findByRiderId(anyString()))
        .thenReturn(Optional.of(riderInactiveTrackingDetails));
    riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
  }


  private static Stream<Arguments> availabilityStatusProvider() {
    return Stream.of(Arguments.of(AvailabilityStatus.Active, AvailabilityStatus.JobInProgress),
        Arguments.of(AvailabilityStatus.Inactive, AvailabilityStatus.JobInProgress),
        Arguments.of(AvailabilityStatus.JobInProgress, AvailabilityStatus.Active),
        Arguments.of(AvailabilityStatus.JobInProgress, AvailabilityStatus.Inactive));
  }
}
