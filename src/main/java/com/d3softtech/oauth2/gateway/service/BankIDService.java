package com.d3softtech.oauth2.gateway.service;

import static com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus.COMPLETE;
import static com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus.PENDING;

import com.d3softtech.oauth2.gateway.client.BankIDClient;
import com.d3softtech.oauth2.gateway.entity.UserAuthenticationRequest;
import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticationStartRequest;
import com.d3softtech.oauth2.gateway.entity.request.StartRequest;
import com.d3softtech.oauth2.gateway.entity.response.StartResponse;
import com.d3softtech.oauth2.gateway.entity.response.StatusResponse;
import com.d3softtech.oauth2.gateway.properties.AuthenticationRequirements;
import com.d3softtech.oauth2.gateway.properties.BankIDConfig;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

@Slf4j
@AllArgsConstructor
@Service
public class BankIDService {

  private final BankIDClient bankIdClient;

  private final BankIDConfig bankIdConfig;
  private Cache userAuthenticationRequest;

  public Mono<StartResponse> startAuthentication(@NonNull String ipAddress, @NonNull StartRequest startRequest) {

    Instant startTime = Instant.now();
    AuthenticationRequirements requirements = bankIdConfig.getAuthenticationRequirements();
    requirements.setPinCode(startRequest.allowFingerPrint());

    return Mono.just(startRequest).map(start -> AuthenticationStartRequest.builder()
            .endUserIp(ipAddress)
            .requirement(requirements)
            .userVisibleData(start.userVisibleData())
            .userNonVisibleData(start.userNonVisibleData())
            .userVisibleDataFormat(start.userVisibleDataFormat())
            .build()
        )
        .transformDeferred(bankIdClient::startAuthentication)
        .map(authenticationStartResponse -> {
          StartResponse startResponse = StartResponse.builder().startRef(
              UUID.randomUUID().toString()).build();

          userAuthenticationRequest.put(startResponse.startRef(),
              UserAuthenticationRequest.builder()
                  .authenticationStartResponse(authenticationStartResponse)
                  .authenticationStatus(PENDING)
                  .lastStatusCheckTime(startTime)
                  .startTime(startTime).build());
          return startResponse;
        }).log(Loggers.getLogger(BankIDService.class));
  }

  public Mono<StatusResponse> checkStatus(@NonNull String startRef) {
    return Mono.fromSupplier(() -> userAuthenticationRequest.get(startRef))
        .filter(this::isStatusCheckAllowed)
        .map(userAuthenticationRequest -> userAuthenticationRequest.getAuthenticationStartResponse().orderRef())
        .transformDeferred(bankIdClient::collect).map(collectResponse -> {
          UserAuthenticationRequest userAuthenticationRequest = userAuthenticationRequest.get(startRef);
          if (COMPLETE.equals(collectResponse.status())) {
            log.info("Completed authentication!");
            userAuthenticationRequest.setEndTime(Instant.now());
            userAuthenticationRequest.setLastStatusCheckTime(Instant.now());

          }
          userAuthenticationRequest.setAuthenticationStatus(collectResponse.status());
          return StatusResponse.builder().status(collectResponse.status()).build();
        }).switchIfEmpty(getCurrentStatus(startRef));

  }

  private Mono<StatusResponse> getCurrentStatus(String startRef) {
    UserAuthenticationRequest userAuthenticationRequest = userAuthenticationRequest.get(startRef);
    log.error("Request with orderRef={} already completed with status={}!",
        userAuthenticationRequest.getAuthenticationStartResponse().orderRef(),
        userAuthenticationRequest.getAuthenticationStatus());
    return Mono.just(StatusResponse.builder().status(userAuthenticationRequest.getAuthenticationStatus()).build());
  }

  private boolean isStatusCheckAllowed(UserAuthenticationRequest userAuthenticationRequest) {
    return userAuthenticationRequest.getAuthenticationStatus().equals(PENDING)
        && Instant.now().minusSeconds(userAuthenticationRequest.getStartTime().getEpochSecond())
        .get(ChronoField.INSTANT_SECONDS) > 60;
  }
}
