package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.CacheExecutionException;
import com.ensolvers.fox.cache.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.CacheSerializingException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MemcachedCache implements Cache {
  private static final String NULL_STRING = "null";

  private final String name;
  private final MemcachedClient memcachedClient;
  private final ObjectMapper objectMapper;
  private final boolean allowNullValues;
  private final int expirationTimeInSeconds;

  public MemcachedCache(
      String name,
      MemcachedClient memcachedClient,
      int expirationTimeInSeconds,
      boolean allowNullValues
  ) {
    this.name = name;
    this.memcachedClient = memcachedClient;
    this.objectMapper = new ObjectMapper();
    this.allowNullValues = allowNullValues;
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return memcachedClient;
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
    return (T) get(key);
  }

  @Override
  public <T> T get(Object key, Callable<T> callable) {
    return (T) get(key);
  }

  @Override
  public void put(Object key, Object value) {
    // Check if is a bulk put or not
    if (CustomCacheKey.class.isInstance(key) && ((CustomCacheKey) key).isBulk()) {
      // value to store must be an instance of Map (key with his value)
      if (!(value instanceof Map)) {
        throw new CacheInvalidArgumentException("[PUT][BULK REQUEST] Expected an instance of Map class in param type");
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
    String finalKey = getMemcachedKey(key);
    memcachedClient.delete(finalKey);
  }

  @Override
  public void clear() {
    memcachedClient.flush();
  }

  /**
   * Build the final key to use in the cache
   * @param key the key of the object
   * @return the final key (a string conformed with the name of the cache and the params of the method)
   */
  private String getMemcachedKey(Object key) {
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
      throw new CacheInvalidArgumentException("[PUT] Cache '" + name + "' is configured to not allow null values but null was provided");
    }

    String finalKey = getMemcachedKey(key);
    try {
      String serializedValue = NULL_STRING;
      if (value != null) {
        serializedValue = objectMapper.writeValueAsString(value);
      }
      memcachedClient.set(finalKey, expirationTimeInSeconds, serializedValue);
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(finalKey, value.getClass(), e);
    }
  }

  private ValueWrapper getSingle(Object key) {
    // Get cached object
    String memcachedKey = getMemcachedKey(key);
    String hit = (String) memcachedClient.get(memcachedKey);

    // Missed hit
    if (hit == null) {
      return null;
    }

    if (hit.equals(NULL_STRING)) {
      return new SimpleValueWrapper(null);
    }

    Object deserializedObject = deserializeUsingReturnType((CustomCacheKey) key, memcachedKey, hit);
    return new SimpleValueWrapper(deserializedObject);
  }

  private ValueWrapper getBulk(CustomCacheKey customCacheKey) {
    // Check that return type is subclass of Map
    if (!Map.class.isAssignableFrom(customCacheKey.getMethod().getReturnType())) {
      throw new CacheInvalidArgumentException("[GET][BULK REQUEST] Expected an instance of Map class in return type");
    }

    // Get the collection of requested keys
    Collection<Object> collection = (Collection<Object>) customCacheKey.getParams()[0] ;

    // Convert key to memcached key
    Map<String, Object> memcachedKeyToOriginalKey = collection.stream()
        .collect(Collectors.toMap(this::getMemcachedKey, Function.identity(), (v1, v2) -> v1));
    Map<Object, Object> result = new HashMap<>();

    // Get cached objects
    Map<String, Object> hits = memcachedClient.getBulk(memcachedKeyToOriginalKey.keySet());

    // Deserialize cached objects
    hits.forEach((memcachedKey, hit) -> {
      Object deserializedObject = null;
      if (!hit.equals(NULL_STRING)) {
        deserializedObject = deserializeUsingMapArgumentType(customCacheKey, memcachedKey, (String) hit);
      }
      result.put(memcachedKeyToOriginalKey.get(memcachedKey), deserializedObject);
      memcachedKeyToOriginalKey.remove(memcachedKey);
    });

    // Check missed hits
    if (!memcachedKeyToOriginalKey.isEmpty()) {
      // Create a new instance of the collection class and collect the missed keys to pass it to the annotated method
      Collection missedKeys;
      try {
        missedKeys = (Collection)customCacheKey.getParams()[0].getClass().getDeclaredConstructor().newInstance();
        missedKeys.addAll(memcachedKeyToOriginalKey.values());
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
        this.putSingle(missedKey, missedHits.get(missedKey));
        result.put(missedKey, missedHits.get(missedKey));
      });
    }

    // Return the result
    return new SimpleValueWrapper(result);
  }
}
