package com.d3softtech.oauth2.gateway.entity;

import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticationStartResponse;
import com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthenticationRequest implements Serializable {

  private AuthenticationStartResponse authenticationStartResponse;
  private CollectStatus authenticationStatus;
  private Instant startTime;
  private Instant lastStatusCheckTime;
  private Instant endTime;
}
