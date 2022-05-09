package com.scb.rider.repository;


import com.scb.rider.model.document.AppConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppConfigRepository extends MongoRepository<AppConfig, String> {

}
