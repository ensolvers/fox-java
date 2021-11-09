package com.ensolvers.fox.cache.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    public Map<String, Profile> profilesWithCacheAndSimpleParams(List<String> paramList) {
        Map<String, Profile> result = new HashMap<>();
        for (String param: paramList) {
            Media media = new Media();
            media.setId(new Random().nextLong());
            media.setTitle(UUID.randomUUID().toString());

            Profile profile = new Profile();
            profile.setId(new Random().nextLong());
            profile.setName(UUID.randomUUID().toString());
            profile.setMedia(media);

            result.put(param, profile);
        }

        return result;
    }

    @Cacheable("profile")
    public Map<String, Profile> profilesWithCacheAndSimpleParams(Set<String> paramList) {
        Map<String, Profile> result = new HashMap<>();
        for (String param: paramList) {
            Media media = new Media();
            media.setId(new Random().nextLong());
            media.setTitle(UUID.randomUUID().toString());

            Profile profile = new Profile();
            profile.setId(new Random().nextLong());
            profile.setName(UUID.randomUUID().toString());
            profile.setMedia(media);

            result.put(param, profile);
        }

        return result;
    }

    @Cacheable("test")
    public String stringNumber(String param1, String param2) {
        return "5";
    }

    @CachePut(value = "test", key = "{#param1, #param2}")
    public String decreaseStringNumber(String param1, String param2, String stringNumber) {
        return String.valueOf(Integer.parseInt(stringNumber) - 1);
    }

    @CacheEvict(value = "test", allEntries=true)
    public void invalidateAll() {

    }

    @CacheEvict(value = "test")
    public void invalidateWithParam(String param1) {

    }
}
