package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.support.SimpleValueWrapper;

/**
 * Spring Cache compatible implementation using Guava as ahe underlying
 * in-memory cache
 */
public class SpringGuavaCache implements org.springframework.cache.Cache {
	private static final String NULL_STRING = "null";

	private final String name;
	private final Cache<String, Object> guavaCache;
	private final boolean allowNullValues;

	public SpringGuavaCache(String name, long expirationTimeInSeconds, boolean allowNullValues) {
		this.name = name;
		this.guavaCache = CacheBuilder.newBuilder().expireAfterAccess(expirationTimeInSeconds, TimeUnit.SECONDS).build();
		this.allowNullValues = allowNullValues;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return guavaCache;
	}

	@Override
	public ValueWrapper get(Object key) {
		Object result = this.guavaCache.asMap().get(computeKey(key));
		if (result == null) {
			return null;
		}
		if (result.equals(NULL_STRING)) {
			return new SimpleValueWrapper(null);
		}
		return new SimpleValueWrapper(result);
	}

	@Override
	public <T> T get(Object key, Class<T> aClass) {
		ValueWrapper wrapper = this.get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public <T> T get(Object key, Callable<T> callable) {
		ValueWrapper wrapper = this.get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public void put(Object key, Object value) {
		if (value == null) {
			if (!allowNullValues) {
				throw new CacheInvalidArgumentException("Cache '" + name + "' is configured to not allow null values but null was provided");
			} else {
				guavaCache.put(computeKey(key), NULL_STRING);
				return;
			}
		}

		guavaCache.put(computeKey(key), value);
	}

	@Override
	public void evict(Object key) {
		guavaCache.invalidate(computeKey(key));
	}

	@Override
	public void clear() {
		guavaCache.invalidateAll();
	}

	/**
	 * Build the final key to use in the cache
	 * @param key the key of the object
	 * @return the final key (a string conformed with the name of the cache and the params of the method)
	 */
	private String computeKey(Object key) {
		StringBuilder finalKeyBuilder = new StringBuilder();
		finalKeyBuilder.append(name);

		if (key instanceof CustomCacheKey) {
			if (((CustomCacheKey)key).isEmpty()) {
				finalKeyBuilder.append("-").append("UNIQUE");
			} else {
				finalKeyBuilder.append("-").append(key);
			}
		} else if (key instanceof Collection) {
			((Collection)key).forEach(o -> finalKeyBuilder.append("-").append(o));
		} else {
			finalKeyBuilder.append("-").append(key);
		}

		return finalKeyBuilder.toString().replace(" ", "-");
	}
}
