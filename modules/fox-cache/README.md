## fox-cache

Provides Memcached and Redis typed cache clients which simplify access to most common cache methods, serialization (using Jackson), and so on


Multiple underlying implementations exist, namely:

* Memcached 
    * [MemcachedCache](./src/main/java/com/ensolvers/fox/cache/memcached/MemcachedCache.java): as the normal implementation
    * [MemcachedBulkCache](./src/main/java/com/ensolvers/fox/cache/memcached/MemcachedBulkCache.java): the bulk loading implementation
* [Redis](./src/main/java/com/ensolvers/fox/cache/redis/RedisCacheFactory.java): a factory to create any type of redis cache on demand.
* [Guava](./src/main/java/com/ensolvers/fox/cache/guava/GuavaCache.java): a simple Guava cache implementation
* [Spring](./src/main/java/com/ensolvers/fox/cache/spring/GenericCacheManager.java): which provides a interface for Spring that works with the specified cache(s) that are used in your project
