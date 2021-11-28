package com.ensolvers.fox.cache;

public class CacheExecutionException extends CacheException {
    public CacheExecutionException(String message) {
        super(message);
    }

    public CacheExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
