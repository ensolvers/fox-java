package com.ensolvers.fox.cache.spring.key;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class CustomCacheKey implements Serializable {
  private final Object target;
  private final Method method;
  private final Object[] params;
  private transient int hashCode;

  public CustomCacheKey(Object target, Method method, Object... elements) {
    Assert.notNull(elements, "Elements must not be null");
    this.target = target;
    this.method = method;
    this.params = elements.clone();
    this.hashCode = Arrays.deepHashCode(this.params);
  }

  public boolean isBulk() {
    return params.length == 1 && Collection.class.isInstance(params[0]);
  }

  public Object[] getParams() {
    return params;
  }

  public boolean isEmpty() {
    return params.length == 0;
  }

  public boolean equals(@Nullable Object other) {
    return this == other
           || (other instanceof CustomCacheKey && Arrays.deepEquals(this.params, ((CustomCacheKey)other).params));
  }

  public final int hashCode() {
    return this.hashCode;
  }

  public String toString() {
    return StringUtils.arrayToDelimitedString(this.params, "-");
  }

  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    this.hashCode = Arrays.deepHashCode(this.params);
  }

  public Method getMethod() {
    return method;
  }

  public Object getTarget() {
    return target;
  }
}
