package com.bantunes82.meeting.schedule.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache configuration using Caffeine for in-memory caching.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    @Value("${cache.calendars.size}")
    private int cacheSize;

    @Value("${cache.calendars.expire-after-write-minutes}")
    private int expireAfterWriteMinutes;

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("calendars");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(Duration.ofMinutes(expireAfterWriteMinutes)));
        return cacheManager;
    }
}
