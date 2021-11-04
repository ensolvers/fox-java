package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.services.logging.Logger;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class SimpleCache extends AbstractValueAdaptingCache {
   private String name;
   private Map  map;

   public SimpleCache(String name) {
      super(true);
      map = Collections.synchronizedMap(new HashMap());
      this.name = name;
      Logger.info(this, "SimpleCache with name: " + name + " initialized" );
   }

   @Override
   protected Object lookup(Object o) {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called lookup in cache: " + name + " for key: " + o);
      }
      return map.get(o);
   }

   @Override
   public <T> T get(Object key, Class<T> aClass) {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called get with class in cache: " + name + " for key: " + key);
      }
      return (T) map.get(key);
   }

   @Override
   public <T> T get(Object key, Callable<T> valueLoader) {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called get with loader in cache: " + name + " for key: " + key);
      }
      return (T) map.get(key);
   }

   @Override
   public void put(Object key, Object value) {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called put in cache: " + name + " for key: " + key);
      }
      map.put(key, value);
   }

   @Override
   public void evict(Object key) {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called evict in cache: " + name + " for key: " + key);
      }
      map.remove(key);
   }

   @Override
   public void clear() {
      if (Logger.isDebugEnabled(this)) {
         Logger.debug(this, "Called clear in cache: " + name);
      }
      map.clear();
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public Object getNativeCache() {
      return map;
   }
}