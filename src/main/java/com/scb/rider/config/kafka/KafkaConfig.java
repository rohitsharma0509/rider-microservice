package com.scb.rider.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.ResourceUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${kafka.rider-status-update-partitions}")
    private Integer riderStatusUpdatePartitions;

    @Value("${kafka.replicas}")
    private Integer replicas;

    @Value("${kafka.notification-topic}")
    private String notificationTopic;

    @Value("${kafka.sms-topic}")
    private String smsTopic;

    @Value("${kafka.rider-status-topic}")
    private String riderStatustopic;
    
    @Value("${kafka.rider-availability-topic}")
    private String riderAvailabilityTopic;

    @Value("${kafka.rider-status-update-topic}")
    private String riderStatusUpdateTopic;

    @Value("${kafka.notification-partitions}")
    private Integer notificationPartitions;

    @Value("${kafka.rider-job-status-partitions}")
    private Integer riderJobStatusPartitions;

    @Value("${kafka.rider-availability-partitions}")
    private Integer riderAvailabilityPartitions;
    
    @Bean
    public NewTopic riderStatusTopic() {

        return TopicBuilder.name(riderStatustopic)
                .partitions(riderJobStatusPartitions)
                .replicas(replicas).build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(notificationTopic)
                .partitions(notificationPartitions)
                .replicas(replicas).build();
    }

    @Bean
    public NewTopic smsTopic() {
        return TopicBuilder.name(smsTopic)
                .partitions(notificationPartitions)
                .replicas(replicas).build();
    }
    
    @Bean
    public NewTopic riderAvailabilityTopic() {
        return TopicBuilder.name(riderAvailabilityTopic)
                .partitions(riderAvailabilityPartitions)
                .replicas(replicas).build();
    }

    @Bean
    public NewTopic riderStatusUpdateTopic() {
        return TopicBuilder.name(riderStatusUpdateTopic)
                .partitions(riderStatusUpdatePartitions)
                .replicas(replicas).build();
    }

}
