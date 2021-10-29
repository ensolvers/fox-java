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
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A simple memcached-based cache that supports caching the same object under a group of keys
 *
 * @param <T> Type of objects that will be stored in the cache
 * @param <K> Enum indicating the group of key types that the cache accepts
 * @author Diego Abel Prince (diego@ensolvers.com)
 */
public class MemcachedMultiKeyBulkCache<T, K extends Enum<K>> {
  Logger logger = LoggerFactory.getLogger(MemcachedMultiKeyBulkCache.class);

  protected final MemcachedClient memcachedClient;
  protected final Map<K, Function<Collection<String>, Map<String, T>>> fetchFunctions;
  protected final String keyPrefix;
  protected final K keyGroup;
  protected final JavaType objectType;
  protected final int expirationTimeInSeconds;

  private final ObjectMapper objectMapper;
  private Function<T, String> customSerializer;
  private Function<String, T> customDeserializer;

  /**
   * Creates a cache instance that allows to store single objects
   * Accepts the class of the object (simple objects)
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunctions the functions to fetch the underlying object by the appropriated key if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param keyGroup an enum indicating the group of key types that the cache accepts
   * @param objectClass type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedMultiKeyBulkCache(
      MemcachedClient memcachedClient,
      Map<K, Function<Collection<String>, Map<String, T>>> fetchFunctions,
      String keyPrefix,
      K keyGroup,
      Class<T> objectClass,
      int expirationTimeInSeconds) {
    this.memcachedClient = memcachedClient;
    this.fetchFunctions = fetchFunctions;
    this.keyPrefix = keyPrefix;
    this.keyGroup = keyGroup;
    this.objectMapper = new ObjectMapper();
    this.objectType = this.objectMapper.getTypeFactory().constructType(objectClass);
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  /**
   * Creates a cache instance that allows to store single objects
   * Accepts a object type factory for "composite" object types e.g: List<Integer>
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunctions the functions to fetch the underlying object by the appropriated key if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param keyGroup an enum indicating the group of key types that the cache accepts
   * @param objectTypeFactory when using the (default) Jackson serializer and complex objects (for
   *     instance, those that have parametric types like Lists) needed to be stored in the cache,
   *     given a Jackson TypeFactory, this function should return the final type
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedMultiKeyBulkCache(
      MemcachedClient memcachedClient,
      Map<K, Function<Collection<String>, Map<String, T>>> fetchFunctions,
      String keyPrefix,
      K keyGroup,
      Function<TypeFactory, JavaType> objectTypeFactory,
      int expirationTimeInSeconds) {
    this.memcachedClient = memcachedClient;
    this.fetchFunctions = fetchFunctions;
    this.keyPrefix = keyPrefix;
    this.keyGroup = keyGroup;
    this.objectMapper = new ObjectMapper();
    this.objectType = objectTypeFactory.apply(this.objectMapper.getTypeFactory());
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  /**
   * Adds custom serializer/deserializer
   *
   * @param memcachedClient the memcached client used to store the objects
   * @param fetchFunctions the functions to fetch the underlying object by the appropriated key if not found in the cache
   * @param keyPrefix the prefix that will be used to create the keys - since several caches can use
   *     the same memcached instance, it is important that every one has its own prefix to avoid
   *     collisions
   * @param keyGroup an enum indicating the group of key types that the cache accepts
   * @param objectClass type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   * @param customSerializer serializer that will be use
   * @param customDeserializer custom deserializer.
   */
  public MemcachedMultiKeyBulkCache(
      MemcachedClient memcachedClient,
      Map<K, Function<Collection<String>, Map<String, T>>> fetchFunctions,
      String keyPrefix,
      K keyGroup,
      Class<T> objectClass,
      int expirationTimeInSeconds,
      Function<T, String> customSerializer,
      Function<String, T> customDeserializer) {

    this(memcachedClient, fetchFunctions, keyPrefix, keyGroup, objectClass, expirationTimeInSeconds);
    this.customSerializer = customSerializer;
    this.customDeserializer = customDeserializer;
  }

  /**
   * Uses the fetch lambda Functions
   *
   * @param keys a collection of keys
   * @param keyGroup the group of the key
   * @return the object
   */
  public Map<String, T> getMap(Collection<String> keys, K keyGroup) {
    // Filter duplicated keys
    Set<String> keySet = new HashSet<>(keys);

    // Computation of keys keeping the correspondence with the original version
    Map<String, String> computedKeyToKey = keySet.stream().collect(
        Collectors.toMap(k ->  this.computeKey(k, keyGroup), Function.identity())
    );

    // Get cached objects computedKey -> object (String)
    Map<String, Object> hits = this.memcachedClient.getBulk(computedKeyToKey.keySet());

    // Map to return key -> object (T)
    Map<String, T> objects = new HashMap<>(keys.size());

    // Convert hits to objects (T)
    hits.forEach((computedKey, value) -> {
      try {
        objects.put(
            computedKeyToKey.get(computedKey),
            this.convertToObject((String) value)
        );
      } catch (IOException e) {
        logger.error(
            "Error when trying to parse object with "
                + "key: ["
                + computedKey
                + "], "
                + "type: ["
                + this.objectType.getTypeName()
                + "], "
                + "content: ["
                + value
                + "]",
            e);
      } finally {
        // Remove the hit
        computedKeyToKey.remove(computedKey);
      }
    });

    // Check hits missed
    if (!computedKeyToKey.isEmpty()) {
      logger.info(
          "Cache missed for {} objects for class {}",
          computedKeyToKey.size(),
          this.objectType.getTypeName());

      // Get the fetch function for the corresponding group
      Function<Collection<String>, Map<String, T>> fetchFunction = fetchFunctions.get(keyGroup);

      // cache miss, go get the object
      Map<String, T> freshObjects = fetchFunction.apply(computedKeyToKey.values());

      // Save the fresh objects to the cache
      freshObjects.forEach((key, freshObject) -> this.put(key, keyGroup, freshObject));

      // Add fresh objects to the result
      objects.putAll(freshObjects);
    }

    return objects;
  }

  /**
   * Uses the fetch lambda Functions
   *
   * @param key The key of the object
   * @param keyGroup the group of the key
   * @return the object
   */
  public T get(String key, K keyGroup) {
    String computedKey = this.computeKey(key, keyGroup);
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

    // Get the fetch function for the corresponding group
    Function<Collection<String>, Map<String, T>> fetchFunction = fetchFunctions.get(keyGroup);

    // cache miss, go get the object
    Map<String, T> freshObjects = fetchFunction.apply(List.of(key));

    return this.put(key, keyGroup, freshObjects.get(key));
  }

  /**
   * Refreshes
   *
   * @param key The key of the object
   * @param keyGroup the group of the key
   * @param freshObject the object
   * @return the object
   */
  public T refresh(String key, K keyGroup, T freshObject) {
    this.invalidate(key, keyGroup);
    this.put(key, keyGroup, freshObject);
    return freshObject;
  }

  /**
   * Method for add new object (distinct from refresh) to the cache
   *
   * @param key The key of the object
   * @param freshObject the object
   * @return the object
   */
  public T put(String key, K keyGroup, T freshObject) {
    try {
      this.memcachedClient.add(
          this.computeKey(key, keyGroup), this.expirationTimeInSeconds, this.convertToString(freshObject));
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

  public void invalidate(String key, K keyGroup) {
    String finalKey = this.computeKey(key, keyGroup);
    this.memcachedClient.delete(finalKey);
  }

  protected String computeKey(String key, K keyGroup) {
    return this.keyPrefix + "-" + keyGroup + "-" + key;
  }
}
