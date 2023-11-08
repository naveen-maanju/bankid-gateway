package com.d3softtech.oauth2.gateway.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AuthenticationRequirements {

  private boolean pinCode;
  private boolean mrtd;
  private String cardReader;
  private List<String> certificatePolicies;
  private Integer personalNumber;

}
