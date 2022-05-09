package com.scb.rider;

import com.scb.rider.tracing.tracer.EnableBasicTracer;
import com.scb.rider.tracing.tracer.logrequest.EnableRequestLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableRequestLog
@EnableBasicTracer
public class RegistrationApplication {
	public static void main(String[] args) {
		SpringApplication.run(RegistrationApplication.class, args);
	}
}
