package com.ensolvers.fox.cache;

public class CacheInvalidArgumentException extends CacheException {
  public CacheInvalidArgumentException(String message) {
    super(message);
  }

  public CacheInvalidArgumentException(String message, Throwable cause) {
    super(message, cause);
  }
}
