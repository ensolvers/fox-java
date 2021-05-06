package com.ensolvers.fox.cache.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RedisCacheFactory {
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> redis;
    private List<String> caches;

    public RedisCacheFactory(RedisClient client) {
        caches = new ArrayList<>();
        this.client = client;
        this.connection = client.connect();
        this.redis = connection.sync();
    }


    /**
     * Creates a new RedisCache if there are serializers for the Key class and Value Class.
     *
     * @param name       of the cache, serves as "topic"
     * @param expireTime time in seconds for the elements in the cache to expire.
     * @param valueClass Class of the values.
     * @param type       of the RedisCache. More info on RedisCacheType.
     * @param <V>        Class of the values.
     */
    public <V> RedisCache<V> getCache(String name, int expireTime, Class<V> valueClass, RedisCacheType type) {
        return this.getCache(name, expireTime, valueClass, type, null, null);
    }

    /**
     *
     * @param name       of the cache, serves as "topic"
     * @param expireTime time in seconds for the elements in the cache to expire.
     * @param valueClass Class of the values.
     * @param type       of the RedisCache. More info on RedisCacheType.
     * @param customSerializer custom serializer that will be used instead of default.
     * @param customDeserializer custom deserializer that will be used instead of default.
     * @param <V>        Class of the values.
     * @return
     */
    public <V> RedisCache<V> getCache(String name, int expireTime, Class<V> valueClass, RedisCacheType type, Function<V, String> customSerializer, Function<String, V> customDeserializer) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("Cache name cannot be empty");
        if (!caches.contains(name)) {
            RedisCache<V> cache = type.getCache(redis, name,expireTime,valueClass,customSerializer,customDeserializer);
            caches.add(name);
            return cache;
        } else {
            throw new InvalidParameterException("Cache with name " + name + " already exist");
        }
    }


    /**
     * Closes the connection to Redis and shutdown the client.
     */
    public void destroy() {
        this.connection.close();
        this.client.shutdown();
    }
}
