package com.scb.rider.service.document;

import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scb.rider.client.NewsPromotionFeignClient;
import com.scb.rider.client.NotificationFeignClient;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderFireBaseNotificationDetailsDto;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderProfileRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderDeviceService {
	@Autowired
	private RiderDeviceDetailRepository riderDeviceDetailRepository;
	@Autowired
	private RiderProfileRepository riderProfileRepository;
	@Autowired
	private NotificationFeignClient notificationFeignClient;

	@Autowired
	private NewsPromotionFeignClient newsPromotionFeignClient;

	public RiderDeviceDetails saveRiderDeviceInfo(String riderId, RiderDeviceDetails riderDevice) {
		RiderProfile riderProfile = riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));

		Optional<RiderDeviceDetails> riderDeviceDetail = riderDeviceDetailRepository
				.findByProfileId(riderProfile.getId());
		RiderDeviceDetails riderResponse = notificationFeignClient.getDeviceArn(riderDevice);

		String deviceArn = riderResponse.getArn();

		RiderDeviceDetails finalRiderDeviceDetail;
			
		if (riderDeviceDetail.isPresent()) {
			finalRiderDeviceDetail = riderDeviceDetail.get();
			finalRiderDeviceDetail.setDeviceToken(riderDevice.getDeviceToken());
			finalRiderDeviceDetail.setArn(deviceArn);
			finalRiderDeviceDetail.setPlatform(riderDevice.getPlatform());
			finalRiderDeviceDetail.setIosDeviceToken(riderDevice.getIosDeviceToken());
		} else {
			finalRiderDeviceDetail = RiderDeviceDetails.builder().arn(deviceArn)
					.deviceToken(riderDevice.getDeviceToken()).platform(riderDevice.getPlatform()).profileId(riderId)
					.iosDeviceToken(riderDevice.getIosDeviceToken())
					.build();
		}
		RiderDeviceDetails saveRiderDeviceDetails = riderDeviceDetailRepository.save(finalRiderDeviceDetail);
		subscribeFireBaseNotificationTopic(riderProfile,riderDevice, "");
		return saveRiderDeviceDetails;
	}

	public Optional<RiderDeviceDetails> findRiderDeviceDetails(String riderId) {
		return riderDeviceDetailRepository.findByProfileId(riderId);
	}

	public void subscribeFireBaseNotificationTopic(RiderProfile riderProfile, RiderDeviceDetails riderDeviceDetails, String prevZoneId) {
		
		
		String iosToken = riderDeviceDetails.getPlatform().name().equalsIgnoreCase(Platform.APNS.name())?riderDeviceDetails.getIosDeviceToken():riderDeviceDetails.getDeviceToken();
		
		String deviceToken = iosToken==null?riderDeviceDetails.getDeviceToken():iosToken;
		log.info("subscribingFireBaseNotificationTopic-" + riderProfile.getRiderId() + "deviceToken-" + deviceToken);
		RiderPreferredZones riderPreferredZonesData = riderProfile.getRiderPreferredZones();
		
		if (ObjectUtils.isNotEmpty(riderPreferredZonesData) && !riderProfile.getStatus().equals(RiderStatus.UNAUTHORIZED)) {

			RiderFireBaseNotificationDetailsDto riderFireBaseNotificationDetailsDto = RiderFireBaseNotificationDetailsDto
					.builder().deviceToken(deviceToken).newPreferredZoneId(riderPreferredZonesData.getPreferredZoneId())
					.previousPreferredZoneId(prevZoneId).build();

			newsPromotionFeignClient.registerDeviceToTopic(riderProfile.getRiderId(), riderFireBaseNotificationDetailsDto);
			log.info("subscribed for FireBaseNotificationTopic-" + riderProfile.getRiderId() + "deviceToken-" + deviceToken);
		}

	}
}
