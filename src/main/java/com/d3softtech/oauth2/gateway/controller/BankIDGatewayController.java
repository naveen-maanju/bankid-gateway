package com.d3softtech.oauth2.gateway.controller;

import com.d3softtech.oauth2.gateway.entity.response.StatusResponse;
import com.d3softtech.oauth2.gateway.entity.request.StartRequest;
import com.d3softtech.oauth2.gateway.entity.response.StartResponse;
import com.d3softtech.oauth2.gateway.service.BankIDService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(path = "/authn", consumes = {"text/plain", "application/*"})
public class BankIDGatewayController {

  private final BankIDService bankIdService;

  @PostMapping("/start")
  @ResponseStatus(HttpStatus.OK)
  public Mono<StartResponse> startAuthentication(@RequestBody StartRequest startRequest, ServerHttpRequest request) {

    String ipAddress = "194.168.10.10";
    log.info("Received request to start the bankid login with ipaddress={}", ipAddress);
    return bankIdService.startAuthentication(ipAddress, startRequest);
  }

  @GetMapping("/{startRef}")
  public Mono<StatusResponse> checkStatus(@NonNull @PathVariable("startRef") String startRef) {
    log.info("Checking authentication status for startRef={}", startRef);
    return bankIdService.checkStatus(startRef);
  }
}
