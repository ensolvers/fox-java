package com.ensolvers.fox.cache.redis;

import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

class RedisCacheFactoryTest extends RedisCacheFactory {
	public RedisCacheFactoryTest(RedisClient client) {
		super(client);
	}

	public void removeCacheFromList(String cacheName) {
		this.caches = this.caches.stream().filter(name -> !name.equals(cacheName)).collect(Collectors.toList());
	}

	@Test
	void shouldGetRegularCache() {
		getRegularCache("testCache", 30000, String.class);
		Assertions.assertTrue(true);
	}
}
