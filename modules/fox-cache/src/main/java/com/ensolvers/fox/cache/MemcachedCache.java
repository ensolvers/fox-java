package com.ensolvers.fox.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

/**
 * A simple memcached-based cache
 *
 * @param <T> Type of objects that will be stored in the cache
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
public class MemcachedCache<T> {

    Logger logger = LoggerFactory.getLogger(MemcachedCache.class);

    protected ObjectMapper mapper;

    protected final MemcachedClient memcachedClient;
    protected final Function<String, T> fetchFunction;
    protected final String keyPrefix;
    protected final TypeReference<T> objectClass;
    protected final int expirationTimeInSeconds;

    private Function<T, String> customSerializer;
    private Function<String, T> customDeserializer;

    private static String NULL_VALUE = "null";

    /**
     * Creates a cache instance that allows to store single objects
     *
     * @param memcachedClient         the cache instance
     * @param fetchFunction           the function to fetch the object
     * @param keyPrefix               the prefix that will be used to create the key
     * @param objectClass             the object class which model will be stored. The class MUST have a default
     *                                constructor for jackson
     * @param expirationTimeInSeconds the default expiration time
     */
    public MemcachedCache(
            MemcachedClient memcachedClient,
            Function<String, T> fetchFunction,
            String keyPrefix,
            TypeReference<T> objectClass,
            int expirationTimeInSeconds) {
        this.memcachedClient = memcachedClient;
        this.fetchFunction = fetchFunction;
        this.keyPrefix = keyPrefix;
        this.objectClass = objectClass;
        this.expirationTimeInSeconds = expirationTimeInSeconds;
    }

    /**
     * Adds custom serializer/deserializer
     *
     * @param memcachedClient         the cache instance
     * @param fetchFunction           the function to fetch the object
     * @param keyPrefix               the prefix that will be used to create the key
     * @param objectClass             the object class which model will be stored. The class MUST have a default
     *                                constructor for jackson
     * @param expirationTimeInSeconds the default expiration time
     * @param customSerializer        custom serializer.
     * @param customDeserializer      custom deserializer.
     */
    public MemcachedCache(
            MemcachedClient memcachedClient,
            Function<String, T> fetchFunction,
            String keyPrefix,
            TypeReference<T> objectClass,
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
        String serializedObject = this.memcachedClient.get(this.computeKey(key)).toString();

        // return the object
        if (serializedObject != null) {
            try {
                return this.convertToObject(serializedObject);
            } catch (Exception e) {
                logger.error(
                        "Error when trying to parse object with " +
                                "key: [" + this.computeKey(key) + "], " +
                                "type: [" + this.objectClass.getType().getTypeName() + "], " +
                                "content: [" + serializedObject + "]", e);
            }
        }

        // cache miss, go get the object, save as null
        T freshObject = fetchFunction.apply(key);

        return this.put(key, freshObject);
    }

    /**
     * Method for refresh an existing entry (distinct from put) to the cache
     *
     * @param key         The key of the object
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
     * @param key         The key of the object
     * @param freshObject the object
     * @return the object
     */
    public T put(String key, T freshObject) {
        try {
            this.memcachedClient.add(
                    this.computeKey(key), this.expirationTimeInSeconds, this.convertToString(freshObject));
        } catch (JsonProcessingException e) {
            logger.error("Error when trying to serialize object with " +
                            "key [" + key + "], " +
                            "type: [" + this.objectClass.getType().getTypeName() + "]",
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
        return this.mapper.writeValueAsString(object);
    }

    protected T convertToObject(String object) throws IOException {
        // If custom deserializer has been provided, use it...
        if (this.customDeserializer != null) {
            return this.customDeserializer.apply(object.toString());
        }

        // ... otherwise, use Jackson
        return this.mapper.readValue(object.toString(), this.objectClass);
    }

    public void invalidate(String key) {
        String finalKey = this.computeKey(key);
        this.memcachedClient.delete(finalKey);
    }

    protected String computeKey(String key) {
        return this.keyPrefix + "-" + key;
    }

}
