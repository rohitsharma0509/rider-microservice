package com.scb.rider.config.security;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AWSCognitoCredentialsProvider {

    @Bean
    @SneakyThrows
    public AWSCognitoIdentityProvider awsCredentialsProvider(){
        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(Regions.AP_SOUTHEAST_1).build();

    }

    @SneakyThrows
    private String sanitize(byte[] strBytes) {
        return new String(strBytes)
                .replace("\r", "")
                .replace("\n", "");
    }
}
