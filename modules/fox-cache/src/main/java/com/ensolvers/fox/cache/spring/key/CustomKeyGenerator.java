package com.ensolvers.fox.cache.spring.key;

import org.springframework.cache.interceptor.KeyGenerator;
import java.lang.reflect.Method;

public class CustomKeyGenerator implements KeyGenerator {
  @Override
  public Object generate(Object target, Method method, Object... params) {
    if (params.length == 0) {
      return new CustomCacheKey(target, method);
    } else {
      return new CustomCacheKey(target, method, params);
    }
  }
}
