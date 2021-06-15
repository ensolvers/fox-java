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

    @Override
    public void push(String key, V value) {
        super.push(key, value);
    }

    public void push(String key, V value, boolean expire) {
    notNull(value);
    this.push(key, Collections.singletonList(value), expire);
  }

  public void push(String key, Collection<V> values, boolean expire) {
    notNull(key);
    notEmpty(values);
    this.redisTransaction(
        () -> {
          this.redis.lpush(this.computeKey(key), this.collectionOfVToStringArray(values));

          if (expire) {
            this.redis.expire(this.computeKey(key), expirationTime);
          }

          return null;
        });
  }

}
