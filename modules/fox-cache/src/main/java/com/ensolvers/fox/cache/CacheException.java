package com.ensolvers.fox.cache;

public abstract class CacheException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CacheException() {
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
