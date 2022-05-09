package com.scb.rider.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RedisHash("RiderTokenEntity")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RiderTokenCachedEntity {
	
	@Id
	private String phoneNumber;
	@Indexed
	private String eventId;

	private boolean loggedOut;
}