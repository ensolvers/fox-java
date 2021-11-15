package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpringRedisCache implements Cache {
  private static final String NULL_STRING = "null";
  private static final int KEY_SCAN_MAX_LIMIT = 1000;

  private final String name;
  private final RedisCommands<String, String> redisClient;
  private final ObjectMapper objectMapper;
  private final boolean allowNullValues;
  private final int expirationTimeInSeconds;

  public SpringRedisCache(
      String name,
      RedisCommands<String, String> redisClient,
      int expirationTimeInSeconds,
      boolean allowNullValues
  ) {
    this.name = name;
    this.redisClient = redisClient;
    this.objectMapper = new ObjectMapper();
    this.expirationTimeInSeconds = expirationTimeInSeconds;
    this.allowNullValues = allowNullValues;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return redisClient;
  }

  @Override
  public ValueWrapper get(Object key) {
    // Check if is a bulk get or not
    if (CustomCacheKey.class.isInstance(key) && ((CustomCacheKey) key).isBulk()) {
      return getBulk((CustomCacheKey)key);
    } else {
      return getSingle(key);
    }
  }

  @Override
  public <T> T get(Object key, Class<T> aClass) {
    ValueWrapper wrapper = get(key);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public <T> T get(Object key, Callable<T> callable) {
    ValueWrapper wrapper = get(key);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public void put(Object key, Object value) {
    // Check if is a bulk put or not
    if (CustomCacheKey.class.isInstance(key) && ((CustomCacheKey) key).isBulk()) {
      // value to store must be an instance of Map (key with his value)
      if (!(value instanceof Map)) {
        throw new CacheInvalidArgumentException("Expected an instance of Map class in param type");
      }

      for (String k: (Collection<? extends String>) ((CustomCacheKey)key).getParams()[0]) {
        putSingle(k, ((Map<?, ?>) value).get(k));
      }
    } else {
      putSingle(key, value);
    }
  }

  @Override
  public void evict(Object key) {
    String finalKey = getRedisKey(key);
    redisClient.del(finalKey);
  }

  @Override
  public void clear() {
    KeyScanCursor<String> cursor;
    do {
      cursor = redisClient.scan(ScanArgs.Builder.limit(KEY_SCAN_MAX_LIMIT).match(name + "-" + "*"));
      if (cursor.getKeys() != null && cursor.getKeys().size() > 0) {
        redisClient.del(cursor.getKeys().toArray(new String[0]));
      }
    } while (!cursor.isFinished() && cursor.getKeys() != null && cursor.getKeys().size() > 0);
  }

  private String getRedisKey(Object key) {
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

  private Object deserializeUsingMapArgumentType(CustomCacheKey customCacheKey, String memcachedKey, String hit) {
    // Get the type of the serialized object
    Type type = ((ParameterizedType) customCacheKey.getMethod().getGenericReturnType()).getActualTypeArguments()[1];
    try {
      return objectMapper.readValue(hit, objectMapper.getTypeFactory().constructType(type));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(memcachedKey, type.getTypeName(), e);
    }
  }

  private Object deserializeUsingReturnType(CustomCacheKey customCacheKey, String memcachedKey, String hit) {
    // Get the type of the serialized object
    Type type = customCacheKey.getMethod().getGenericReturnType();
    try {
      return objectMapper.readValue(hit, objectMapper.getTypeFactory().constructType(type));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(memcachedKey, type.getTypeName(), e);
    }
  }

  private void putSingle(Object key, Object value) {
    // Check null value
    if ((!allowNullValues) && value == null) {
      throw new CacheInvalidArgumentException("Cache '" + name + "' is configured to not allow null values but null was provided");
    }

    String finalKey = getRedisKey(key);
    try {
      String serializedValue = NULL_STRING;
      if (value != null) {
        serializedValue = objectMapper.writeValueAsString(value);
      }
      redisClient.set(finalKey, serializedValue, SetArgs.Builder.ex(expirationTimeInSeconds));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(finalKey, value.getClass(), e);
    }
  }

  private ValueWrapper getSingle(Object key) {
    // Get cached object
    String redisKey = getRedisKey(key);
    String hit = redisClient.get(redisKey);

    // Missed hit
    if (hit == null) {
      return null;
    }

    if (hit.equals(NULL_STRING)) {
      return new SimpleValueWrapper(null);
    }

    Object deserializedObject = deserializeUsingReturnType((CustomCacheKey) key, redisKey, hit);
    return new SimpleValueWrapper(deserializedObject);
  }

  private ValueWrapper getBulk(CustomCacheKey customCacheKey) {
    // Check that return type is subclass of Map
    if (!Map.class.isAssignableFrom(customCacheKey.getMethod().getReturnType())) {
      throw new CacheInvalidArgumentException("Expected an instance of Map class in return type");
    }

    // Get the collection of requested keys
    Collection<Object> collection = (Collection<Object>) customCacheKey.getParams()[0] ;

    // Convert key to memcached key
    Map<String, Object> redisKeyToOriginalKey = collection.stream()
        .collect(Collectors.toMap(this::getRedisKey, Function.identity(), (v1, v2) -> v1));
    Map<Object, Object> result = new HashMap<>();

    // Get cached objects
    Map<String, Object> hits;
    if (redisKeyToOriginalKey.isEmpty()) {
      hits = new HashMap<>();
    } else {
      hits = redisClient.mget(redisKeyToOriginalKey.keySet().toArray(new String[0])).stream()
                .filter(Value::hasValue)
                .collect(Collectors.toMap(KeyValue::getKey, kv -> kv.getValueOrElse(null)));
    }

    // Deserialize cached objects
    hits.forEach((memcachedKey, hit) -> {
      Object deserializedObject = null;
      if (!hit.equals(NULL_STRING)) {
        deserializedObject = deserializeUsingMapArgumentType(customCacheKey, memcachedKey, (String) hit);
      }
      result.put(redisKeyToOriginalKey.get(memcachedKey), deserializedObject);
      redisKeyToOriginalKey.remove(memcachedKey);
    });

    // Check missed hits
    if (!redisKeyToOriginalKey.isEmpty()) {
      // Create a new instance of the collection class and collect the missed keys to pass it to the annotated method
      Collection missedKeys;
      try {
        missedKeys = (Collection)customCacheKey.getParams()[0].getClass().getDeclaredConstructor().newInstance();
        missedKeys.addAll(redisKeyToOriginalKey.values());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw CacheInvalidArgumentException.collectionError(customCacheKey.getParams()[0].getClass(), e);
      }

      // Execute the method to retrieve the missed hits
      Map missedHits;
      try {
        missedHits = (Map)customCacheKey.getMethod().invoke(customCacheKey.getTarget(), missedKeys);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new CacheExecutionException("Error trying to execute annotated method. Check stack trace for more information.", e);
      }

      // Cache the missed hits and add to the result
      missedKeys.forEach(missedKey -> {
        putSingle(missedKey, missedHits.get(missedKey));
        result.put(missedKey, missedHits.get(missedKey));
      });
    }

    // Return the result
    return new SimpleValueWrapper(result);
  }
}
