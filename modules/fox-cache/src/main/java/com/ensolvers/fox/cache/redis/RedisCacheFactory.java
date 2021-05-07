package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.cache.CacheInitializationException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
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
     * @param <V>        Class of the values.
     */
    public <V, C extends RedisCache<V>> C getCache(String name, int expireTime, Class<V> valueClass, Class<C> cacheType) {
        return this.getCache(name, expireTime, valueClass, cacheType, null, null);
    }

    /**
     * Creates a new cache if there's no other cache with the same name already created.
     *
     * @param name               of the cache, serves as "topic"
     * @param expireTime         time in seconds for the elements in the cache to expire.
     * @param valueClass         Class of the values.
     * @param cacheType          Class of the new cache.
     * @param customSerializer   custom serializer that will be used instead of default.
     * @param customDeserializer custom deserializer that will be used instead of default.
     * @return a new cache.
     */
    public <V, C extends RedisCache<V>> C getCache(
            String name,
            int expireTime,
            Class<V> valueClass,
            Class<C> cacheType,
            Function<V, String> customSerializer,
            Function<String, V> customDeserializer) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Cache name cannot be empty");
        }
        if (!caches.contains(name)) {
            try {
                C cache = cacheType
                        .getDeclaredConstructor(RedisCommands.class, String.class, int.class, Class.class, Function.class, Function.class)
                        .newInstance(redis, name, expireTime, valueClass, customSerializer, customDeserializer);
                caches.add(name);
                return cache;
            } catch (Exception e) {
                throw new CacheInitializationException("There was a problem when initializing cache with name: " + name, e);
            }
        }
        throw new InvalidParameterException("Cache with name " + name + " already exist");
    }

    /**
     * Closes the connection to Redis and shutdown the client.
     */
    public void destroy() {
        this.connection.close();
        this.client.shutdown();
    }
}
