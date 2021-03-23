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

import java.io.IOException;
import java.net.InetSocketAddress;
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
    }

    @BeforeEach
    public void initializeCache() throws IOException {
        this.memcachedClient = new MemcachedClient(new InetSocketAddress(11211));
    }

    @Test
    void testSingleObjectCache() throws InterruptedException {
        AtomicInteger fetchCount = new AtomicInteger();
        AtomicLong nextId = new AtomicLong();

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
        // to the fetching method
        cache.get("2");
        assertEquals(2, fetchCount.get());
    }

}