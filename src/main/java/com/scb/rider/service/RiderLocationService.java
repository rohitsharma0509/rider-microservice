package com.scb.rider.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.DistanceResponseEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiderLocationService {

	@Autowired
	LocationServiceFeignClient locationServiceFeignClient;
	
	@Autowired
	OperationFeignClient operationFeignClient;
	
	public boolean checkDistance(Double longitudeFrom, Double latitudeFrom, Double longitudeTo, Double latitudeTo) {

		ResponseEntity<DistanceResponseEntity> distance = locationServiceFeignClient.getDistance(longitudeFrom,
				latitudeFrom, longitudeTo, latitudeTo);

		ConfigDataResponse findbyKey = operationFeignClient.getConfigData("distanceFromMerchant");

		log.info("calculated distance in meters-{}", distance.getBody().getDistance());
		
		if(ObjectUtils.isNotEmpty(findbyKey))
		{

			double defaultDis = Double.parseDouble(findbyKey.getValue());

			return distance.getBody() != null ? (distance.getBody().getDistance() > defaultDis ? false : true) : false;

		}
		return false;
		
	}

	public void deleteRiderLocation(String riderId){
		log.info("Deleting rider details from location, riderId:{}", riderId);
		try {
			locationServiceFeignClient.deleteRider(riderId);
		}catch (Exception exception){
			log.error("Error invoking location api for deletion", exception);
		}

	}
}