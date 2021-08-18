package com.ensolvers.fox.cache.redis;

import io.lettuce.core.RedisClient;
import java.util.stream.Collectors;

public class RedisCacheFactoryTest extends RedisCacheFactory {
  public RedisCacheFactoryTest(RedisClient client) {
    super(client);
  }

  public void removeCacheFromList(String cacheName) {
    this.caches =
        this.caches.stream().filter(name -> !name.equals(cacheName)).collect(Collectors.toList());
  }
}
