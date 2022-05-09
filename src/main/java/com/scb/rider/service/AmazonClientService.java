package com.scb.rider.service;

import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.regions.Regions;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.s3.S3Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Region;
import javax.annotation.PostConstruct;

public class AmazonClientService {

  // AmazonS3 Client, in this object you have all AWS API calls about S3.
  private AmazonS3 s3Client;


  // Your bucket name.
  @Value("${amazon.s3.bucket-name}")
  private String bucketName;
  
  @Value("${amazon.s3.external-bucket-name}")
  private String externalBucketName;

  protected String getBucketName() {
    return bucketName;
  }
  
  protected String getExternalBucketName() {
    return externalBucketName;
  }

  // Getters for parents.
  protected AmazonS3 getClient() {
    return s3Client;
  }

  // This method are called after Spring starts AmazonClientService into your container.
  @PostConstruct
  private void init() {

    Regions clientRegion = Regions.AP_SOUTHEAST_1;

    this.s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();

  }
}
