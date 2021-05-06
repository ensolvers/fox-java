package com.ensolvers.fox.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.IOException;
import java.util.function.Function;

public class RedisRegularCache<V> extends RedisCache<V> {

    public RedisRegularCache(
            RedisCommands<String, String> redis,
            String name,
            int expirationTime,
            Class<V> valueClass,
            Function<V, String> customSerializer,
            Function<String, V> customDeserializer) {
        super(redis, name, expirationTime, valueClass, customSerializer, customDeserializer);
    }


    /**
     * Given a key returns the associated value. Return null if there's no such key.
     *
     * @param key The key of the element to retrieve.
     * @return The value associated with the key.
     */
    public V get(String key) {
        try {
            return this.deserializeValue(this.redis.get(this.computeKey(key)));
        } catch (JsonProcessingException e) {
            logger.error("Error when trying to serialize key", e);
        } catch (IOException e) {
            logger.error("Error when trying to deserialize value", e);
        }
        return null;
    }


    /**
     * Sets a value to a key. Overrides previous value if key already exists.
     * Sets TTL.
     *
     * @param key   The key of the element to store.
     * @param value The element to store.
     */
    public void set(String key, V value) {
        notNull(key);
        notNull(value);
        try {
            this.redis.set(this.computeKey(key), this.serializeValue(value), new SetArgs().ex(expirationTime));
        } catch (JsonProcessingException e) {
            logger.error("Error when trying to serialize key or value", e);
        }
    }


}
