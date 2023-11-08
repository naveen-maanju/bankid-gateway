package com.d3softtech.oauth2.gateway.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ApplicationConfig {


  @Bean
  public Cache userAuthenticationRequest(CacheManager cacheManager) {
    return cacheManager.getCache("user-authentication-request");
  }
}
