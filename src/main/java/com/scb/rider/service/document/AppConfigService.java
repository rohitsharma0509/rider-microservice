package com.scb.rider.service.document;

import com.scb.rider.model.document.AppConfig;
import com.scb.rider.repository.AppConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class AppConfigService {
    @Autowired
    private AppConfigRepository appConfigRepository;

    @Cacheable(value = "appConfigCache", key = "#root.methodName", cacheManager = "appConfigCacheManager")
    public AppConfig getAppConfig() {
        log.info("getAppConfig method starts. updating cache");
        List<AppConfig> configurations = appConfigRepository.findAll();
        if(!CollectionUtils.isEmpty(configurations)) {
            log.info("application configuration size {}", configurations.size());
            return configurations.get(0);
        } else {
            return null;
        }
    }
}
