package com.d3softtech.oauth2.gateway;

import com.d3softtech.oauth2.gateway.properties.BankIDConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BankIDConfig.class)
public class BankIdGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(BankIdGatewayApplication.class, args);
  }
}