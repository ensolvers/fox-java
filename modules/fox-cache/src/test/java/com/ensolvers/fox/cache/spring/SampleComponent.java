package com.ensolvers.fox.cache.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;

@SpringBootApplication
@Component
public class SampleComponent {

    public String timeWithoutCache() {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("test")
    public String timeWithCache() {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("test")
    public String timeWithCacheAndSimpleParams(String param1) {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("test")
    public String timeWithCacheAndSimpleParams(String param1, Integer param2) {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("test")
    public String timeWithCacheAndSimpleParams(String param1, Integer param2, boolean param3) {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("test")
    public String timeWithCacheAndSimpleParams(String param1, Integer param2, boolean param3, Date param4) {
        return java.time.LocalDateTime.now().toString();
    }

    @Cacheable("profile")
    public Profile profileWithCacheAndSimpleParams(String param1) {
        Media media = new Media();
        media.setId(new Random().nextLong());
        media.setTitle(UUID.randomUUID().toString());

        Profile profile = new Profile();
        profile.setId(new Random().nextLong());
        profile.setName(UUID.randomUUID().toString());
        profile.setMedia(media);

        return profile;
    }

    @Cacheable("profile")
    public List<Profile> profilesWithCacheAndSimpleParams(String param1) {
        Media media = new Media();
        media.setId(new Random().nextLong());
        media.setTitle(UUID.randomUUID().toString());

        Profile profile = new Profile();
        profile.setId(new Random().nextLong());
        profile.setName(UUID.randomUUID().toString());
        profile.setMedia(media);

        return List.of(profile);
    }

    @CacheEvict(value = "test", allEntries=true)
    public void invalidateAll() {

    }

    @CacheEvict(value = "test")
    public void invalidateWithParam(String param1) {

    }
}
