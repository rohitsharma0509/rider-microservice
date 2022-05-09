package com.scb.rider.service.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.scb.rider.kafka.publisher.RiderStatusUpdateKafkaPublisher;
import com.scb.rider.model.document.RiderProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RiderProfileUpdaterServiceTest {

   
    @InjectMocks
    private RiderProfileUpdaterService riderProfileUpdaterService;

	@Mock
	RiderStatusUpdateKafkaPublisher riderStatusUpdateKafkaPublisher;
    
    @Test
    void shouldPublish() {
		riderProfileUpdaterService.publish(new RiderProfile());
		verify(riderStatusUpdateKafkaPublisher, times(1)).publish(any(RiderProfile.class));
    
    }
    
    @Test
    void shouldPublishNew() {
		riderProfileUpdaterService.publish(new RiderProfile());
        verify(riderStatusUpdateKafkaPublisher, times(1)).publish(any(RiderProfile.class));

    }
   
}