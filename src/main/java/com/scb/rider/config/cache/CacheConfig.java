package com.scb.rider.config.cache;

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static final int CACHE_EXPIRY_TIME_IN_MINUTES = 1;

    @Bean("appConfigCacheManager")
    @Override
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(final String name) {
                return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(CACHE_EXPIRY_TIME_IN_MINUTES, TimeUnit.MINUTES)
                        .build().asMap(), Boolean.TRUE);
            }
        };

        cacheManager.setCacheNames(Arrays.asList("appConfigCache"));
        return cacheManager;
    }

}
