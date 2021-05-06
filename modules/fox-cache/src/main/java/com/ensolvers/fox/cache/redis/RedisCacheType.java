package com.ensolvers.fox.cache.redis;

import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.Function;

public enum RedisCacheType {
    /**
     * Regular cache. Used to store Key -> Value
     */
    REGULAR() {
        @Override
        public <V> RedisCache<V> getCache(
                RedisCommands<String, String> redis,
                String name,
                int expirationTime,
                Class<V> valueClass,
                Function<V, String> customSerializer,
                Function<String, V> customDeserializer) {
            return new RedisRegularCache<>(redis, name, expirationTime, valueClass, customSerializer, customDeserializer);
        }
    },
    /**
     * List cache. Used to store Key -> List<value>
     */
    LIST {
        @Override
        public <V> RedisCache<V> getCache(
                RedisCommands<String, String> redis,
                String name,
                int expirationTime,
                Class<V> valueClass,
                Function<V, String> customSerializer,
                Function<String, V> customDeserializer) {
            return new RedisListCache<>(redis, name, expirationTime, valueClass, customSerializer, customDeserializer);
        }
    },
    /**
     * Set cache. Used to store Key -> Set<Value>.
     * Sets doesn't allow repeated elements.
     */
    SET {
        @Override
        public <V> RedisCache<V> getCache(
                RedisCommands<String, String> redis,
                String name,
                int expirationTime,
                Class<V> valueClass,
                Function<V, String> customSerializer,
                Function<String, V> customDeserializer) {
            return new RedisSetCache<>(redis, name, expirationTime, valueClass, customSerializer, customDeserializer);

        }
    };

    public abstract <V> RedisCache<V> getCache(
            RedisCommands<String, String> redis,
            String name,
            int expirationTime,
            Class<V> valueClass,
            Function<V, String> customSerializer,
            Function<String, V> customDeserializer);
}
