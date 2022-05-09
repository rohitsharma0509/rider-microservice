
package com.scb.rider.util;

import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.enumeration.RiderJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class KafkaPublisherTest {
	
	@Mock
	private KafkaTemplate<String, RiderJobStatusEventModel> kafkaTemplate;
	
	KafkaPublisher kafkaPublisher;
	private String riderId ;
	private String jobId; 
	private ZonedDateTime dateTime;
	private RiderJobStatus jobStatus;
	
	@BeforeEach
	public void setup() {
		kafkaPublisher = new KafkaPublisher(kafkaTemplate, "mock-topic");
		riderId = "R0101";
		jobId = "J001001";
		dateTime = ZonedDateTime.now();
		jobStatus = RiderJobStatus.JOB_ACCEPTED;
	}
	@Test
	public void testPublishSuccess() throws InterruptedException, ExecutionException {
		RiderJobStatusEventModel model = new RiderJobStatusEventModel(riderId,jobId, dateTime.toString(), jobStatus, "RR001");
		ListenableFuture future = Mockito.mock(ListenableFuture.class);
	    Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(RiderJobStatusEventModel.class))).thenReturn(future);
	    kafkaPublisher.publish(model);
		verify(kafkaTemplate, times(1)).send(Mockito.anyString(), Mockito.anyString()
				, Mockito.any(RiderJobStatusEventModel.class));
	}
	
}