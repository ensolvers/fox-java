package com.ensolvers.fox.cache.spring.key;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class KeyGeneratorConfig extends CachingConfigurerSupport {
  @Bean
  public KeyGenerator keyGenerator() {
    return new CustomKeyGenerator();
  }
}
