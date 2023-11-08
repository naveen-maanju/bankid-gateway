package com.d3softtech.oauth2.gateway.properties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties("bankid")
public class BankIDConfig {

  String baseUrl;
  AuthenticationRequirements authenticationRequirements;
}
