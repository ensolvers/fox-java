package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.CacheSerializingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

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
    String finalKey = getFinalKey(key);
    String serializedObject = (String) memcachedClient.get(finalKey);

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
    String finalKey = this.getFinalKey(key);
    try {
      String serializedObject = this.objectMapper.writeValueAsString(value);
      this.memcachedClient.add(finalKey, this.expirationTimeInSeconds, serializedObject);
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

  @Override
  public void evict(Object key) {
    String finalKey = this.getFinalKey(key);
    this.memcachedClient.delete(finalKey);
  }

  @Override
  public void clear() {
    // Disabled because this.memcachedClient.flush() will delete all content in the memcached service (global)
  }

  private String getFinalKey(Object key) {
    return name + "-" + key.toString();
  }
}
