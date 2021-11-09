package com.ensolvers.fox.cache.spring;

import com.ensolvers.fox.cache.spring.context.config.SimpleCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {SimpleCacheConfig.class, SampleComponent.class})
public class SimpleCacheTest {
    @Autowired
    SampleComponent sampleComponent;

    @Test
    void testGet() {
        CacheTester.testGet(sampleComponent);
    }

    @Test
    void testGetComplexObjects() {
        CacheTester.testGetComplexObjects(sampleComponent);
    }

    @Test
    void testPut() {
        CacheTester.testPut(sampleComponent);
    }

    @Test
    void testInvalidate() {
        CacheTester.testInvalidate(sampleComponent);
    }
}
