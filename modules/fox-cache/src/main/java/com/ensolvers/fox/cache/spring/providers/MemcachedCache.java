package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.CacheSerializingException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class MemcachedCache implements Cache {
  private String name;
  private final MemcachedClient memcachedClient;
  private final ObjectMapper objectMapper;
  private final JavaType objectType;
  private final boolean allowNullValues;
  private final int expirationTimeInSeconds;

  public MemcachedCache(
      String name,
      MemcachedClient memcachedClient,
      Function<TypeFactory, JavaType> typeBuilder,
      int expirationTimeInSeconds,
      boolean allowNullValues
  ) {
    this.name = name;
    this.memcachedClient = memcachedClient;
    this.objectMapper = new ObjectMapper();
    this.objectType = typeBuilder.apply(this.objectMapper.getTypeFactory());
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
    if (key instanceof CustomCacheKey && ((CustomCacheKey) key).isBulk()) {
      return getBulk((CustomCacheKey)key);
    } else {
      return getSingle(key);
    }
  }

  @Override
  public <T> T get(Object key, Class<T> aClass) {
    return (T) this.get(key);
  }

  @Override
  public <T> T get(Object key, Callable<T> callable) {
    return (T) this.get(key);
  }

  @Override
  public void put(Object key, Object value) {
    if (key instanceof CustomCacheKey && ((CustomCacheKey) key).isBulk()) {
      if (!(value instanceof Map)) {
        throw new CacheInvalidArgumentException("[PUT][BULK REQUEST] Expected an instance of Map class");
      }

      for (String k: (Iterable<? extends String>) ((CustomCacheKey)key).getParams()[0]) {
        putSingle(k, ((Map<?, ?>) value).get(k));
      }
    } else {
      putSingle(key, value);
    }
  }

  @Override
  public void evict(Object key) {
    String finalKey = this.getFinalKey(key);
    this.memcachedClient.delete(finalKey);
  }

  @Override
  public void clear() {
    this.memcachedClient.flush();
  }

  private ValueWrapper getBulk(CustomCacheKey customCacheKey) {
    Set<String> finalKeys = new HashSet<>();
    Map<String, Object> finalKeyToOriginalKey = new HashMap<>();
    Map<Object, Object> deserializedObjects = new HashMap<>();

    Iterable iterable = (Iterable<Object>) customCacheKey.getParams()[0] ;
    iterable.forEach(key -> {
      String finalKey = getFinalKey(key);
      finalKeys.add(finalKey);
      finalKeyToOriginalKey.put(finalKey, key);
    });

    Map<String, Object> serializedObjects = this.memcachedClient.getBulk(finalKeys);

    if (finalKeys.size() == serializedObjects.size()) {
      serializedObjects.forEach((finalKey, value) -> {
        try {
          Object deserializedObject = this.objectMapper.readValue((String)value, objectType);
          deserializedObjects.put(
              finalKeyToOriginalKey.get(finalKey),
              deserializedObject
          );
        } catch (JsonProcessingException e) {
          throw new CacheSerializingException(
              "Error when trying to deserialize object with "
                  + "key ["
                  + finalKey
                  + "], "
                  + "type: ["
                  + objectType.getTypeName()
                  + "]",
              e);
        }
      });
      return new SimpleValueWrapper(deserializedObjects);
    } else {
      return null;
    }
  }

  private ValueWrapper getSingle(Object key) {
    String finalKey = getFinalKey(key);
    String serializedObject = (String) memcachedClient.get(finalKey);

    if (serializedObject == null) {
      return null;
    }

    try {
      Object deserializedObject = this.objectMapper.readValue(serializedObject, objectType);
      return new SimpleValueWrapper(deserializedObject);
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException(
          "Error when trying to deserialize object with "
              + "key ["
              + finalKey
              + "], "
              + "type: ["
              + objectType.getTypeName()
              + "]",
          e);
    }
  }

  private void putSingle(Object key, Object value) {
    String finalKey = this.getFinalKey(key);
    try {
      String serializedObject = this.objectMapper.writeValueAsString(value);
      this.memcachedClient.set(finalKey, this.expirationTimeInSeconds, serializedObject);
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException(
          "Error when trying to serialize object with "
              + "key ["
              + finalKey
              + "], "
              + "type: ["
              + objectType.getTypeName()
              + "]",
          e);
    }
  }

  private String getFinalKey(Object key) {
    StringBuilder finalKeyBuilder = new StringBuilder();
    finalKeyBuilder.append(name);

    if (key instanceof CustomCacheKey) {
      if (((CustomCacheKey)key).isEmpty()) {
        finalKeyBuilder.append("-").append("UNIQUE");
      } else {
        finalKeyBuilder.append("-").append(key);
      }
    } else if (key instanceof Iterable) {
      var iterable = (Iterable<Object>) key;
      iterable.forEach(o -> finalKeyBuilder.append("-").append(o));
    } else {
      finalKeyBuilder.append("-").append(key);
    }

    return finalKeyBuilder.toString().replace(" ", "-");
  }
}
