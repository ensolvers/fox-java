package com.ensolvers.fox.cache.core;

/**
 * Generic cache interface to abstract underlying implementation
 *
 * @param <T> type of elements stored in the cache
 */
public interface GenericCache<T> {

  /**
   * Obtains a new element
   *
   * @param key returns an element from the given key
   * @return the cached element
   */
  T get(String key) throws Exception;

  /**
   * Removes the entry matching with {@code key} in the cache
   *
   * @param key
   */
  void refresh(String key) throws Exception;

  /**
   * Stores an item in the cache
   *
   * @param key the key under which the item will be stored
   * @param object the item to be stored
   */
  void put(String key, T object) throws Exception;
}
