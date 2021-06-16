/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.cache.memcached;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.function.Function;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple memcached-based cache
 *
 * @param <T> Type of objects that will be stored in the cache
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
public class MemcachedCache<T> {
  Logger logger = LoggerFactory.getLogger(MemcachedCache.class);

  protected final MemcachedClient memcachedClient;
  protected final Function<String, T> fetchFunction;
  protected final String keyPrefix;
  protected final JavaType objectType;
  protected final int expirationTimeInSeconds;

  private final ObjectMapper objectMapper;
  private Function<T, String> customSerializer;
  private Function<String, T> customDeserializer;

  /**
   * Creates a cache instance that allows to store single objects
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunction the function to fetch the underlying object if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param objectClass type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedCache(
      MemcachedClient memcachedClient,
      Function<String, T> fetchFunction,
      String keyPrefix,
      Class<T> objectClass,
      int expirationTimeInSeconds) {
    this.memcachedClient = memcachedClient;
    this.fetchFunction = fetchFunction;
    this.keyPrefix = keyPrefix;
    this.objectMapper = new ObjectMapper();
    this.objectType = this.objectMapper.getTypeFactory().constructType(objectClass);
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  /**
   * Creates a cache instance that allows to store single objects
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunction the function to fetch the underlying object if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param objectTypeFactory when using the (default) Jackson serializer and complex objects (for
   *     instance, those that have parametric types like Lists) needed to be stored in the cache,
   *     given a Jackson TypeFactory, this function should return the final type
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedCache(
      MemcachedClient memcachedClient,
      Function<String, T> fetchFunction,
      String keyPrefix,
      Function<TypeFactory, JavaType> objectTypeFactory,
      int expirationTimeInSeconds) {
    this.memcachedClient = memcachedClient;
    this.fetchFunction = fetchFunction;
    this.keyPrefix = keyPrefix;
    this.objectMapper = new ObjectMapper();
    this.objectType = objectTypeFactory.apply(this.objectMapper.getTypeFactory());
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  /**
   * Adds custom serializer/deserializer
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunction the function to fetch the underlying object if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param objectClass type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   * @param customSerializer serializer that will be use
   * @param customDeserializer custom deserializer.
   */
  public MemcachedCache(
      MemcachedClient memcachedClient,
      Function<String, T> fetchFunction,
      String keyPrefix,
      Class<T> objectClass,
      int expirationTimeInSeconds,
      Function<T, String> customSerializer,
      Function<String, T> customDeserializer) {

    this(memcachedClient, fetchFunction, keyPrefix, objectClass, expirationTimeInSeconds);
    this.customSerializer = customSerializer;
    this.customDeserializer = customDeserializer;
  }

  /**
   * Uses the fetch lambda Function
   *
   * @param key The key of the object
   * @return the object
   */
  public T get(String key) {
    String computedKey = this.computeKey(key);
    String serializedObject = (String) this.memcachedClient.get(computedKey);

    // return the object
    if (serializedObject != null) {
      try {
        return this.convertToObject(serializedObject);
      } catch (Exception e) {
        logger.error(
            "Error when trying to parse object with "
                + "key: ["
                + computedKey
                + "], "
                + "type: ["
                + this.objectType.getTypeName()
                + "], "
                + "content: ["
                + serializedObject
                + "]",
            e);
      }
    }

    // cache miss, go get the object
    T freshObject = fetchFunction.apply(key);

    return this.put(key, freshObject);
  }

  /**
   * Refreshes
   *
   * @param key The key of the object
   * @param freshObject the object
   * @return the object
   */
  public T refresh(String key, T freshObject) {
    this.invalidate(key);
    this.put(key, freshObject);
    return freshObject;
  }

  /**
   * Method for add new object (distinct from refresh) to the cache
   *
   * @param key The key of the object
   * @param freshObject the object
   * @return the object
   */
  public T put(String key, T freshObject) {
    try {
      this.memcachedClient.add(
          this.computeKey(key), this.expirationTimeInSeconds, this.convertToString(freshObject));
    } catch (JsonProcessingException e) {
      logger.error(
          "Error when trying to serialize object with "
              + "key ["
              + key
              + "], "
              + "type: ["
              + this.objectType.getTypeName()
              + "]",
          e);
    }

    return freshObject;
  }

  protected String convertToString(T object) throws JsonProcessingException {
    // If custom serializer has been provided, use it...
    if (this.customSerializer != null) {
      return this.customSerializer.apply(object);
    }

    // ... otherwise, use Jackson
    return this.objectMapper.writeValueAsString(object);
  }

  protected T convertToObject(String serializedObject) throws IOException {
    // If custom deserializer has been provided, use it...
    if (this.customDeserializer != null) {
      return this.customDeserializer.apply(serializedObject);
    }

    // ... otherwise, use Jackson
    return this.objectMapper.readValue(serializedObject, this.objectType);
  }

  public void invalidate(String key) {
    String finalKey = this.computeKey(key);
    this.memcachedClient.delete(finalKey);
  }

  protected String computeKey(String key) {
    return this.keyPrefix + "-" + key;
  }
}