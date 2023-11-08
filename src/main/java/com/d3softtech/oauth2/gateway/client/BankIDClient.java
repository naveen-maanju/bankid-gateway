package com.d3softtech.oauth2.gateway.client;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.d3softtech.oauth2.gateway.entity.UserAuthenticationRequest;
import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticationStartRequest;
import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticationStartResponse;
import com.d3softtech.oauth2.gateway.entity.bankid.CollectResponse;
import com.d3softtech.oauth2.gateway.entity.bankid.ErrorResponse;
import com.d3softtech.oauth2.gateway.exception.BankIDException;
import com.d3softtech.oauth2.gateway.properties.BankIDConfig;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BankIDClient {

  public static final String AUTH_URL = "/rp/v6.0/auth";
  public static final String COLLECT_URL = "/rp/v6.0/collect";
  private final WebClient webClient;
  private static final JsonMapper JSON_MAPPER = new JsonMapper();

  public BankIDClient(WebClient.Builder builder, BankIDConfig bankIdConfig, WebClientSsl ssl) {
    this.webClient = builder.baseUrl(bankIdConfig.getBaseUrl()).apply(ssl.fromBundle("bankid-client")).build();

  }

  public Mono<AuthenticationStartResponse> startAuthentication(
      Mono<AuthenticationStartRequest> authenticationStartRequest) {
    return webClient.post().uri(uriBuilder -> uriBuilder.path(AUTH_URL).build())
        .body(authenticationStartRequest, AuthenticationStartRequest.class)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .retrieve()
        .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
          log.error("Bad request={}", clientResponse);
          return clientResponse.bodyToMono(ErrorResponse.class).map(BankIDException::new);
        })
        .bodyToMono(AuthenticationStartResponse.class);
  }

  public Mono<CollectResponse> collect(Mono<String> orderRef) {
    return webClient.post().uri(uriBuilder -> uriBuilder.path(COLLECT_URL).build())
        .body(orderRef, String.class)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .retrieve()
        .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> {
          log.error("Bad request={}", clientResponse);
          return clientResponse.bodyToMono(ErrorResponse.class).map(BankIDException::new);
        })
        .bodyToMono(CollectResponse.class);
  }
}
