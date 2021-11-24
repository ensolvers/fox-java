package com.ensolvers.fox.cache.guava;

/** Exception thrown by a custom cache */
public class CacheException extends Exception {

	private final transient GenericCache<?> causingCache;

	public CacheException(GenericCache<?> causingCache) {
		this.causingCache = causingCache;
	}

	public CacheException(String message, GenericCache<?> causingCache) {
		super(message);
		this.causingCache = causingCache;
	}

	public CacheException(String message, Throwable cause, GenericCache<?> causingCache) {
		super(message, cause);
		this.causingCache = causingCache;
	}

	public GenericCache<?> getCausingCache() {
		return causingCache;
	}
}
