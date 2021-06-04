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
package com.ensolvers.fox.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemcachedCacheTest {

    private MemcachedClient memcachedClient;

    private static class TestClass {
        Long id;
        String stringValue;
        Integer integerValue;
        Long longValue;

        public TestClass(Long id, String stringValue, Integer integerValue, Long longValue) {
            this.id = id;
            this.stringValue = stringValue;
            this.integerValue = integerValue;
            this.longValue = longValue;
        }

        protected TestClass() {
        }

        public String getStringValue() {
            return stringValue;
        }

        public Integer getIntegerValue() {
            return integerValue;
        }

        public Long getLongValue() {
            return longValue;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestClass testClass = (TestClass) o;

            if (!id.equals(testClass.id)) return false;
            if (!stringValue.equals(testClass.stringValue)) return false;
            if (!integerValue.equals(testClass.integerValue)) return false;
            return longValue.equals(testClass.longValue);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + stringValue.hashCode();
            result = 31 * result + integerValue.hashCode();
            result = 31 * result + longValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "id=" + id +
                    ", stringValue='" + stringValue + '\'' +
                    ", integerValue=" + integerValue +
                    ", longValue=" + longValue +
                    '}';
        }
    }

    @BeforeEach
    public void initializeCache() throws IOException {
        this.memcachedClient = new MemcachedClient(new InetSocketAddress(11211));
    }

    @Test
    void testFetchingAndInvalidation() throws InterruptedException {
        AtomicInteger fetchCount = new AtomicInteger();

        // create simple cache for TestClass
        MemcachedCache<TestClass> cache = new MemcachedCache<TestClass>(
                this.memcachedClient,
                id -> {
                    fetchCount.getAndIncrement();
                    return new TestClass(Long.parseLong(id), "someString", 2, 3L);
                },
                "testClassCache1",
                TestClass.class,
                3);

        // ensure that after several accesses, the object is semantically the same
        TestClass objectWithId1 = cache.get("2");
        assertEquals(objectWithId1, cache.get("2"));
        assertEquals(objectWithId1, cache.get("2"));
        assertEquals(objectWithId1, cache.get("2"));

        // ensure that only 1 call to fetching method has been made, even after 4 accesses
        assertEquals(1, fetchCount.get());

        // wait 5 seconds for item to expire
        Thread.sleep(5000);

        // get the object again and ensure that item expired so we get a second access
        // to the fetching method - however, only 1 call should be made
        cache.get("2");
        cache.get("2");
        cache.get("2");
        assertEquals(2, fetchCount.get());

        // now invalidate the key, access 3 extra times, only 1 extra fetching should be
        // executed
        cache.invalidate("2");
        cache.get("2");
        cache.get("2");
        cache.get("2");
        assertEquals(3, fetchCount.get());
    }

    @Test
    void testAdvancedJacksonSerialization() {
        AtomicInteger fetchCount = new AtomicInteger();

        // create simple cache for TestClass
        MemcachedCache<List<TestClass>> cache = new MemcachedCache<>(
                this.memcachedClient,
                id -> {
                    fetchCount.getAndIncrement();

                    return Arrays.asList(
                            new TestClass(1L, "someString", 1, 1L),
                            new TestClass(2L, "someString", 2, 2L),
                            new TestClass(3L, "someString", 3, 3L));
                },
                "testClassCache2",
                typeFactory -> typeFactory.constructCollectionType(List.class, TestClass.class),
                3);

        cache.get("any");
        cache.get("any");
        List<TestClass> list = cache.get("any");

        // only 1 fetching should have been done if the cache worked properly
        assertEquals(1, fetchCount.get());

        // check that the deserialized list is structurally the same
        assertEquals(3, list.size());
        assertEquals(list.get(0), new TestClass(1L, "someString", 1, 1L));
        assertEquals(list.get(1), new TestClass(2L, "someString", 2, 2L));
        assertEquals(list.get(2), new TestClass(3L, "someString", 3, 3L));
    }

    @Test
    void testCustomSerialization() {
        AtomicInteger fetchCount = new AtomicInteger();

        // create simple cache for TestClass
        MemcachedCache<TestClass> cache = new MemcachedCache<>(
                this.memcachedClient,
                id -> {
                    fetchCount.getAndIncrement();
                    return new TestClass(Long.parseLong(id), "someString", 1, 1L);
                },
                "testClassCache3",
                TestClass.class,
                3,
                // serialize the object using static field order and "|" as a separator
                (TestClass instance) ->
                            instance.getId() + "|" +
                            instance.getStringValue() + "|" +
                            instance.getIntegerValue() + "|" +
                            instance.getLongValue(),
                serialized -> {
                    String[] parts = serialized.split("\\|");
                    return new TestClass(
                            Long.parseLong(parts[0]),
                            parts[1],
                            Integer.parseInt(parts[2]),
                            Long.parseLong(parts[3])
                    );
        });


        cache.get("2");
        cache.get("2");
        TestClass instance = cache.get("2");

        // only 1 fetching should have been done if the cache worked properly
        assertEquals(1, fetchCount.get());

        // check that the deserialized list is structurally the same
        assertEquals(instance, new TestClass(2L, "someString", 1, 1L));
    }



}