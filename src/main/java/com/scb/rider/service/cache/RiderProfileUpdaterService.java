package com.scb.rider.service.cache;

import com.scb.rider.model.document.RiderProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scb.rider.kafka.publisher.RiderStatusUpdateKafkaPublisher;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderProfileUpdaterService {
	@Autowired
	RiderStatusUpdateKafkaPublisher riderStatusUpdateKafkaPublisher;

	public void publish(RiderProfile riderProfile) {
		log.info("publishing data to kafka for rider Id-{}", riderProfile.getRiderId());
		riderStatusUpdateKafkaPublisher.publish(riderProfile);
	}
}
