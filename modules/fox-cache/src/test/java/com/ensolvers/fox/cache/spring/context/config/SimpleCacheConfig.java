package com.ensolvers.fox.cache.spring.context.config;

import com.ensolvers.fox.cache.spring.GenericCacheManager;
import com.ensolvers.fox.cache.spring.providers.SimpleCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
public class SimpleCacheConfig {

  @Bean
  public CacheManager cacheManager() {
    return new GenericCacheManager()
        .append("test", new SimpleCache("test"))
        .append("profile", new SimpleCache("profile"));
  }
}
