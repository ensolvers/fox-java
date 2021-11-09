package com.ensolvers.fox.cache.spring;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CacheTester {
  public static void testGet(SampleComponent sampleComponent) {
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

  public static void testGetComplexObjects(SampleComponent sampleComponent) {
    Profile profile1 = sampleComponent.profileWithCacheAndSimpleParams("profile1");
    Profile profile2 = sampleComponent.profileWithCacheAndSimpleParams("profile2");
    assertEquals(profile1, sampleComponent.profileWithCacheAndSimpleParams("profile1"));
    assertEquals(profile2, sampleComponent.profileWithCacheAndSimpleParams("profile2"));
  }

  public static void testBulkGetComplexObjects(SampleComponent sampleComponent) {
    // Test list
    Map<String, Profile> profiles1 = sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles1a", "profiles1b"));
    Map<String, Profile> profiles2 = sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles2a", "profiles2b"));
    assertEquals(profiles1, sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles1a", "profiles1b")));
    assertEquals(profiles2, sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles2a", "profiles2b")));

    // Test list with repeats
    Map<String, Profile> profiles3 = sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles3a", "profiles3a", "profiles3b"));
    assertEquals(profiles3, sampleComponent.profilesWithCacheAndSimpleParams(List.of("profiles3a", "profiles3a", "profiles3b")));

    // Test set
    Map<String, Profile> profiles4 = sampleComponent.profilesWithCacheAndSimpleParams(Set.of("profiles4a", "profiles4b"));
    assertEquals(profiles4, sampleComponent.profilesWithCacheAndSimpleParams(Set.of("profiles4a", "profiles4b")));
  }

  public static void testPut(SampleComponent sampleComponent) {
    String stringNumber = sampleComponent.stringNumber("stringNumber1a", "stringNumber1b");
    sampleComponent.decreaseStringNumber("stringNumber1a", "stringNumber1b", stringNumber);
    String decreasedStringNumber = sampleComponent.stringNumber("stringNumber1a", "stringNumber1b");
    assertEquals(String.valueOf(Integer.parseInt(stringNumber) - 1), decreasedStringNumber);
  }

  public static void testInvalidate(SampleComponent sampleComponent) {
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
