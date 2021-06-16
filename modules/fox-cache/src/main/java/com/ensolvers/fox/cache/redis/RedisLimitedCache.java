package com.ensolvers.fox.cache.redis;

import io.lettuce.core.api.sync.RedisCommands;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class RedisLimitedCache<V> extends RedisListCache<V> implements RedisCollection<V> {

  public RedisLimitedCache(
      RedisCommands<String, String> redis,
      String name,
      int expirationTime,
      Class<V> valueClass,
      Function<V, String> customSerializer,
      Function<String, V> customDeserializer,
      Integer maxEntriesPerBlock) {
    super(redis, name, expirationTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  // Defaulted to non expiring cache
  @Override
  public void push(String key, V value) {
    this.push(key, value, false);
  }

  // Defaulted to non expiring cache
  @Override
  public void push(String key, Collection<V> values) {
    this.push(key, values, false);
  }

  public void push(String key, V value, boolean expire) {
    notNull(value);
    this.push(key, Collections.singletonList(value), expire);
  }

  public void push(String key, Collection<V> values, boolean expire) {
    notNull(key);
    notEmpty(values);

    boolean keyExists = this.keyExists(key);
    boolean overflowEntries = this.size(key) >= this.maxEntriesPerBlock;

    this.redisTransaction(
        () -> {
          // Takes the first element if block size reached
          if (keyExists && overflowEntries) {
            this.redis.lpop(this.computeKey(key));
          }

          this.redis.lpush(this.computeKey(key), this.collectionOfVToStringArray(values));

          if (expire) {
            this.redis.expire(this.computeKey(key), expirationTime);
          }

          return null;
        });
  }


}
