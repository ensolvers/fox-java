package com.ensolvers.fox.cache.redis;

import static org.junit.jupiter.api.Assertions.*;

import com.ensolvers.fox.cache.TestClass;
import io.lettuce.core.RedisClient;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RedisCacheTest {
  /** Tests ignored until redis instance is setup */
  private static final String REDIS_URI = "redis://localhost:6379/0";

  @AfterEach
  public void clear() {
    factory.destroy();
  }

  RedisClient client;
  RedisCacheFactory factory;

  @BeforeEach
  public void setUp() {
    this.client = RedisClient.create(REDIS_URI);
    this.factory = new RedisCacheFactory(client);
  }

  @Test
  @Disabled
  public void redisRegularCacheTestCase() {
    RedisRegularCache<String> cache =
        this.factory.getRegularCache("testRegularCacheString", 5, String.class);
    RedisRegularCache<String> cache2 =
        this.factory.getRegularCache("testRegularCacheString2", 5, String.class);

    assertFalse(cache.keyExists("testKey-1"));
    cache.set("testKey-1", "testValue-1");
    assertTrue(cache.keyExists("testKey-1"));

    cache.set("testKey-2", "testValue-2");
    cache.set("testKey-3", "testValue-3");
    cache2.set("testKey-1", "testValue-1");
    cache2.set("testKey-2", "testValue-2");

    // Neither the key nor the value can be null
    assertThrows(IllegalArgumentException.class, () -> cache.set("shouldRaiseException", null));
    assertThrows(IllegalArgumentException.class, () -> cache.set(null, null));
    assertThrows(IllegalArgumentException.class, () -> cache.set(null, "shouldRaiseException"));

    assertEquals("testValue-1", cache.get("testKey-1"));
    assertEquals("testValue-2", cache.get("testKey-2"));
    assertEquals("testValue-1", cache.get("testKey-1"));
    assertEquals("testValue-2", cache.get("testKey-2"));

    cache.invalidateAll();
    assertNull(cache.get("testKey-1"));
    assertNull(cache.get("testKey-2"));
    assertNotNull(cache2.get("testKey-1"));
    assertNotNull(cache2.get("testKey-2"));
  }

  @Test
  @Disabled
  public void redisListCacheTestCase() {
    RedisListCache<String> cache =
        this.factory.getListCache("testListCacheString", 1, String.class);
    RedisListCache<String> cache2 =
        this.factory.getListCache("testListCacheString2", 3, String.class);

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-2", "testValue-1");
    cache.push("testKey-2", "testValue-2");
    cache.push("testKey-2", "testValue-3");

    List<String> cache2List = new ArrayList<>();
    cache2List.add("repeatedValue-1");
    cache2List.add("repeatedValue-1");
    cache2.push("testKey-1", cache2List);

    // Neither the key nor the value can be null nor empty
    assertThrows(
        IllegalArgumentException.class, () -> cache.push("shouldRaiseException", (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, "shouldRaiseException"));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, new ArrayList<>()));

    assertEquals(2, cache.get("testKey-1").size());
    assertEquals(3, cache.get("testKey-2").size());
    assertEquals(2, cache2.get("testKey-1").size());

    cache.invalidateAll();
    assertEquals(new ArrayList<>(), cache.get("testKey-1"));
    assertEquals(new ArrayList<>(), cache.get("testKey-2"));
    assertEquals(cache2List, cache2.get("testKey-1"));
    cache2.invalidateAll();
    assertEquals(new ArrayList<>(), cache2.get("testKey-1"));
  }

  @Test
  @Disabled
  public void redisSetCacheTestCase() {
    RedisSetCache<String> cache = this.factory.getSetCache("testSetCacheString", 1, String.class);
    RedisSetCache<String> cache2 = this.factory.getSetCache("testSetCacheString2", 2, String.class);
    Set<String> cache2set = new HashSet<>(Collections.emptySet());
    cache2set.add("repeatedValue-1");

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-2", "testValue-1");
    cache.push("testKey-2", "testValue-2");
    cache.push("testKey-2", "testValue-3");

    List<String> cache2List = new ArrayList<>();
    cache2List.add("repeatedValue-1");
    cache2List.add("repeatedValue-1");
    cache2.push("testKey-1", cache2List);

    // Neither the key nor the value can be null nor empty
    assertThrows(
        IllegalArgumentException.class, () -> cache.push("shouldRaiseException", (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, "shouldRaiseException"));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, new ArrayList<>()));

    assertEquals(2, cache.get("testKey-1").size());
    assertEquals(3, cache.get("testKey-2").size());
    // No repeated values on set
    assertEquals(1, cache2.get("testKey-1").size());

    cache.invalidateAll();
    assertEquals(Collections.EMPTY_SET, cache.get("testKey-1"));
    assertEquals(Collections.EMPTY_SET, cache.get("testKey-2"));
    assertEquals(cache2set, cache2.get("testKey-1"));
    cache2.invalidateAll();
    assertEquals(Collections.EMPTY_SET, cache2.get("testKey-1"));
  }

  @Test
  @Disabled
  public void redisLimitedListCacheTestCase() {
    RedisLimitedCache<String> cache =
            this.factory.getLimitedListCache("testListCacheString", 1, String.class, 5);
    RedisLimitedCache<String> cache2 =
            this.factory.getLimitedListCache("testListCacheString2", 3, String.class, 5);

  }

  @Test
  @Disabled
  public void redisTypesTestCase() {
    RedisRegularCache<Long> longCache;
    RedisRegularCache<Integer> integerCache;
    RedisRegularCache<Character> characterCache;
    RedisRegularCache<Boolean> booleanCache;

    longCache = this.factory.getRegularCache("testRegularCacheLong", 1, Long.class);
    integerCache = this.factory.getRegularCache("testRegularCacheInteger", 1, Integer.class);
    characterCache = this.factory.getRegularCache("testRegularCacheCharacter", 1, Character.class);
    booleanCache = this.factory.getRegularCache("testRegularCacheBoolean", 1, Boolean.class);

    Long longKey = 1L;
    Long longValue = 7L;
    longCache.set(longKey.toString(), longValue);
    assertEquals(longValue, longCache.get(longKey.toString()));

    Integer integerKey = 2;
    Integer integerValue = 5;
    integerCache.set(integerKey.toString(), integerValue);
    assertEquals(integerValue, integerCache.get(integerKey.toString()));

    Character charKey = 'f';
    Character charValue = 'm';
    characterCache.set(charKey.toString(), charValue);
    assertEquals(charValue, characterCache.get(charKey.toString()));

    Boolean boolKey = true;
    Boolean boolValue = false;
    booleanCache.set(boolKey.toString(), boolValue);
    assertFalse(booleanCache.get(boolKey.toString()));
  }

  @Test
  @Disabled
  public void redisTimeoutTestCase() throws InterruptedException {
    RedisRegularCache<String> regularCache;
    RedisListCache<String> listCache;
    RedisSetCache<String> setCache;

    regularCache = this.factory.getRegularCache("testRegularCacheString3", 3, String.class);
    listCache = this.factory.getListCache("testListCacheString2", 3, String.class);
    setCache = this.factory.getSetCache("testSetCacheString3", 3, String.class);

    String regularCacheKey = "regularCacheKey";
    String regularCacheValue = "regularCacheValue";

    String listCacheKey = "listCacheKey";
    String listCacheValue = "listCacheValue";

    String setCacheKey = "setCacheKey";
    String setCacheValue = "setCacheValue";

    regularCache.set(regularCacheKey, regularCacheValue);
    listCache.push(listCacheKey, listCacheValue);
    setCache.push(setCacheKey, setCacheValue);

    assertNotNull(regularCache.get(regularCacheKey));
    assertEquals(1, listCache.get(listCacheKey).size());
    assertEquals(1, setCache.get(setCacheKey).size());

    TimeUnit.SECONDS.sleep(4);

    assertNull(regularCache.get(regularCacheKey));
    assertEquals(0, listCache.get(listCacheKey).size());
    assertEquals(0, setCache.get(setCacheKey).size());

    // Reset TTL test
    regularCache.set(regularCacheKey, regularCacheValue);
    listCache.push(listCacheKey, listCacheValue);
    setCache.push(setCacheKey, setCacheValue);

    TimeUnit.SECONDS.sleep(2);

    regularCache.resetTTL(regularCacheKey);
    listCache.resetTTL(listCacheKey);
    setCache.resetTTL(setCacheKey);

    TimeUnit.SECONDS.sleep(2);

    assertNotNull(regularCache.get(regularCacheKey));
    assertEquals(1, listCache.get(listCacheKey).size());
    assertEquals(1, setCache.get(setCacheKey).size());
  }

  @Test
  @Disabled
  public void redisCustomClassCacheTestCase() {
    RedisRegularCache<TestClass> cache;
    cache = this.factory.getRegularCache("testRegularCacheTestClass", 3, TestClass.class);

    String cacheKey = "regularCacheKey";
    TestClass cacheValue = new TestClass(1L, "someString", 2, 3L);

    cache.set(cacheKey, cacheValue);
    assertEquals(cacheValue, cache.get(cacheKey));
  }

  @Test
  @Disabled
  public void redisCustomSerializerTestCase() {
    RedisRegularCache<TestClass> cache;
    cache =
        this.factory.getRegularCache(
            "testRegularCacheTestClass",
            3,
            TestClass.class,
            // serialize the object using static field order and "|" as a separator
            (TestClass instance) ->
                instance.getId()
                    + "|"
                    + instance.getStringValue()
                    + "|"
                    + instance.getIntegerValue()
                    + "|"
                    + instance.getLongValue(),
            serialized -> {
              String[] parts = serialized.split("\\|");
              return new TestClass(
                  Long.parseLong(parts[0]),
                  parts[1],
                  Integer.parseInt(parts[2]),
                  Long.parseLong(parts[3]));
            });

    String cacheKey = "regularCacheKey";
    TestClass cacheValue = new TestClass(1L, "someString", 2, 3L);

    cache.set(cacheKey, cacheValue);
    assertEquals(cacheValue, cache.get(cacheKey));
  }
}
