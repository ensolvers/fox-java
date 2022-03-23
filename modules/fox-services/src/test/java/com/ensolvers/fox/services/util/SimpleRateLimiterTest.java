package com.ensolvers.fox.services.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleRateLimiterTest {

    @Test
    void testThrottle() throws InterruptedException {
        SimpleRateLimiter simpleRateLimiter = new SimpleRateLimiter(1);

        AtomicInteger invocationCount = new AtomicInteger();

        // try to execute 1k method calls, only 1 should pass
        for (int i = 0; i < 1000; i++) {
            simpleRateLimiter.throttle("key", () -> invocationCount.getAndIncrement());
        }
        assertEquals(1, invocationCount.get());

        // wait for 2 secs until that key expires
        Thread.sleep(2000);

        // now after trying another 1k times, only 2 executions should be executed
        for (int i = 0; i < 1000; i++) {
            simpleRateLimiter.throttle("key", () -> invocationCount.getAndIncrement());
        }
        assertEquals(2, invocationCount.get());
    }
}