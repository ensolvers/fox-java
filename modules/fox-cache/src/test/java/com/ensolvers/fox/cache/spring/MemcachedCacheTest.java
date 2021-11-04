package com.ensolvers.fox.cache.spring;

import com.ensolvers.fox.cache.spring.context.config.MemcachedCacheConfig;
import com.ensolvers.fox.cache.spring.context.config.SimpleCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ContextConfiguration(classes = {MemcachedCacheConfig.class, SampleComponent.class})
@Testcontainers
class MemcachedCacheTest {
    @Container
    public static GenericContainer<?> memcachedContainer =
        new GenericContainer<>(DockerImageName.parse("memcached:1.6.10")).withExposedPorts(11211);

    @Autowired
    SampleComponent sampleComponent;

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("cache.memcache.port", () -> memcachedContainer.getFirstMappedPort());
    }

    @Test
    void testGetAndPut() {
        sampleComponent.invalidateAll();

        String time1a = sampleComponent.timeWithCache();
        String time1b = sampleComponent.timeWithCache();
        assertEquals(time1a, sampleComponent.timeWithCache());
        assertEquals(time1b, sampleComponent.timeWithCache());
        assertEquals(time1a, time1b);

        String time2a = sampleComponent.timeWithCacheAndSimpleParams("time2a");
        String time2b = sampleComponent.timeWithCacheAndSimpleParams("time2b");
        assertEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
        assertEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
        assertNotEquals(time2a, time2b);

        String time3a = sampleComponent.timeWithCacheAndSimpleParams("time3a", 1);
        String time3b = sampleComponent.timeWithCacheAndSimpleParams("time3b", 2);
        assertEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
        assertEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
        assertNotEquals(time3a, time3b);

        String time4a = sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true);
        String time4b = sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false);
        assertEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
        assertEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
        assertNotEquals(time4a, time4b);

        Date date1 = new Date();
        Date date2 = new Date();
        String time5a = sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1);
        String time5b = sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2);
        assertEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
        assertEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));
        assertNotEquals(time5a, time5b);
    }

    @Test
    void testGetAndPutComplexObjects() {
        Profile profile1 = sampleComponent.profileWithCacheAndSimpleParams("profile1");
        Profile profile2 = sampleComponent.profileWithCacheAndSimpleParams("profile2");
        assertEquals(profile1, sampleComponent.profileWithCacheAndSimpleParams("profile1"));
        assertEquals(profile2, sampleComponent.profileWithCacheAndSimpleParams("profile2"));

        List<Profile> profiles1 = sampleComponent.profilesWithCacheAndSimpleParams("profiles1");
        List<Profile> profiles2 = sampleComponent.profilesWithCacheAndSimpleParams("profiles2");
        assertEquals(profiles1, sampleComponent.profilesWithCacheAndSimpleParams("profiles1"));
        assertEquals(profiles2, sampleComponent.profilesWithCacheAndSimpleParams("profiles2"));
    }

    @Test
    void testInvalidate() {
        sampleComponent.invalidateAll();

        String time1a = sampleComponent.timeWithCache();
        assertEquals(time1a, sampleComponent.timeWithCache());

        String time2a = sampleComponent.timeWithCacheAndSimpleParams("time2a");
        String time2b = sampleComponent.timeWithCacheAndSimpleParams("time2b");
        assertEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
        assertEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
        assertNotEquals(time2a, time2b);

        String time3a = sampleComponent.timeWithCacheAndSimpleParams("time3a", 1);
        String time3b = sampleComponent.timeWithCacheAndSimpleParams("time3b", 2);
        assertEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
        assertEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
        assertNotEquals(time3a, time3b);

        String time4a = sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true);
        String time4b = sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false);
        assertEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
        assertEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
        assertNotEquals(time4a, time4b);

        Date date1 = new Date();
        Date date2 = new Date();
        String time5a = sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1);
        String time5b = sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2);
        assertEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
        assertEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));
        assertNotEquals(time5a, time5b);

        // Invalidate all
        sampleComponent.invalidateAll();
        assertNotEquals(time1a, sampleComponent.timeWithCache());
        assertNotEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
        assertNotEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
        assertNotEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
        assertNotEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
        assertNotEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
        assertNotEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
        assertNotEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
        assertNotEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));

        // Invalidate by param
        String time6a = sampleComponent.timeWithCacheAndSimpleParams("time6a");
        String time6b = sampleComponent.timeWithCacheAndSimpleParams("time6b");
        assertEquals(time6a, sampleComponent.timeWithCacheAndSimpleParams("time6a"));
        assertEquals(time6b, sampleComponent.timeWithCacheAndSimpleParams("time6b"));
        sampleComponent.invalidateWithParam("time6a");
        assertNotEquals(time6a, sampleComponent.timeWithCacheAndSimpleParams("time6a"));
        assertEquals(time6b, sampleComponent.timeWithCacheAndSimpleParams("time6b"));
    }
}
