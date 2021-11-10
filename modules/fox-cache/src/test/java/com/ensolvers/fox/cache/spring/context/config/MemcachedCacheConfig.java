package com.ensolvers.fox.cache.spring.context.config;

import com.ensolvers.fox.cache.spring.GenericCacheManager;
import com.ensolvers.fox.cache.spring.Profile;
import com.ensolvers.fox.cache.spring.providers.MemcachedCache;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.InetSocketAddress;

@EnableCaching
public class MemcachedCacheConfig {

  @Bean
  public CacheManager cacheManager(@Value("${cache.memcache.port}") String memcachedPort) throws IOException {
    MemcachedClient client = new MemcachedClient(new InetSocketAddress(Integer.parseInt(memcachedPort)));
    MemcachedCache testCache = new MemcachedCache("test", client, 60000, true);
    MemcachedCache profileCache = new MemcachedCache("profile", client, 60000, true);

    return new GenericCacheManager()
        .append("test", testCache)
        .append("profile", profileCache);
  }
}
