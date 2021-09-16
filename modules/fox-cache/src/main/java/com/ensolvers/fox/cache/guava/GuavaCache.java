package com.ensolvers.fox.cache.guava;

import com.ensolvers.fox.cache.core.CacheException;
import com.ensolvers.fox.cache.core.GenericCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * G
 * @param <T>
 */
public class GuavaCache<T> implements GenericCache<T> {

    private final LoadingCache<String, T> cache;

    public GuavaCache(Function<String, T> fetchingFunction) {
        this.cache = CacheBuilder.newBuilder().build(new CacheLoader<String, T>() {

            @Override
            public T load(String key) throws Exception {
                return fetchingFunction.apply(key);
            }

        });
    }

    @Override
    public T get(String key) throws CacheException {
        try {
            return this.cache.get(key);
        } catch (ExecutionException e) {
            throw new CacheException("Error when trying to get an item from the cache", e, this);
        }
    }

    @Override
    public void refresh(String key) {
        this.cache.refresh(key);
    }

    @Override
    public void put(String key, T object) {
        this.cache.put(key, object);
    }
}
