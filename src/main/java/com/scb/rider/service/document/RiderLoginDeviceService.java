package com.scb.rider.service.document;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderLoginDeviceDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderLoginDeviceDetailRepository;
import com.scb.rider.repository.RiderProfileRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RiderLoginDeviceService {
  @Autowired
  private RiderLoginDeviceDetailRepository riderLoginDeviceDetailRepository;
  @Autowired
  private RiderProfileRepository riderProfileRepository;

  public RiderLoginDeviceDetails saveRiderLoginDeviceInfo(String phoneNumber,
      RiderLoginDeviceDetails riderLoginDevice) {

    Optional<RiderLoginDeviceDetails> riderLoginDeviceDetail =
        riderLoginDeviceDetailRepository.findByPhoneNumber(phoneNumber);

    RiderLoginDeviceDetails finalRiderLoginDeviceDetail;
    if (riderLoginDeviceDetail.isPresent()) {
      finalRiderLoginDeviceDetail = riderLoginDeviceDetail.get();
      finalRiderLoginDeviceDetail.setDeviceId(riderLoginDevice.getDeviceId());
      finalRiderLoginDeviceDetail.setUpdationDateTime(LocalDateTime.now());
    } else {
      finalRiderLoginDeviceDetail = RiderLoginDeviceDetails.builder()
          .deviceId(riderLoginDevice.getDeviceId()).creationDateTime(LocalDateTime.now())
          .updationDateTime(LocalDateTime.now()).phoneNumber(phoneNumber).build();
    }
    return riderLoginDeviceDetailRepository.save(finalRiderLoginDeviceDetail);
  }

  public Optional<RiderLoginDeviceDetails> findRiderLoginDeviceDetails(String phoneNumber) {
    return riderLoginDeviceDetailRepository.findByPhoneNumber(phoneNumber);
  }

  public boolean validateRiderDeviceId(String phoneNumber, String deviceId) {
    Optional<RiderLoginDeviceDetails> riderLoginDeviceDetails = findRiderLoginDeviceDetails(phoneNumber);
    log.info("Fecthing Rider Device Id for Valdiation: {}", phoneNumber);
    if (!riderLoginDeviceDetails.isPresent() || riderLoginDeviceDetails.get().getDeviceId().equals(deviceId)) {
      log.info("Rider Device Id is valid for Rider phoneNumber:{}", phoneNumber);
      return true;
    }
    log.error("Rider Device Id is Invalid for Rider phoneNumber:{}", phoneNumber);
    return false;
  }
}
