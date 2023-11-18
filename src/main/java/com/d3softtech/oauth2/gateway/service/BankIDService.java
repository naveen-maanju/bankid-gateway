package com.d3softtech.oauth2.gateway.service;

import static com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus.CANCELLED;
import static com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus.COMPLETE;
import static com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus.PENDING;

import com.d3softtech.oauth2.gateway.client.BankIDClient;
import com.d3softtech.oauth2.gateway.entity.UserAuthenticationRequest;
import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticatedUser;
import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticationStartRequest;
import com.d3softtech.oauth2.gateway.entity.bankid.CancelResponse;
import com.d3softtech.oauth2.gateway.entity.bankid.ErrorResponse;
import com.d3softtech.oauth2.gateway.entity.request.StartRequest;
import com.d3softtech.oauth2.gateway.entity.response.StartResponse;
import com.d3softtech.oauth2.gateway.entity.response.StatusResponse;
import com.d3softtech.oauth2.gateway.exception.BankIDException;
import com.d3softtech.oauth2.gateway.properties.AuthenticationRequirements;
import com.d3softtech.oauth2.gateway.properties.BankIDConfig;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
  private final AuditService auditService;
  private final OneTimeReferenceService oneTimeReferenceService;
  private CacheManager cacheManager;

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

          getUserAuthenticationRequestCache().put(startResponse.startRef(),
              UserAuthenticationRequest.builder()
                  .authenticationStartResponse(authenticationStartResponse)
                  .authenticationStatus(PENDING)
                  .lastStatusCheckTime(startTime)
                  .startTime(startTime).build());
          return startResponse;
        }).log(Loggers.getLogger(BankIDService.class));
  }

  public Mono<StatusResponse> checkStatus(@NonNull String startRef) {
    return Mono.fromSupplier(() -> getUserAuthenticationRequestCache().get(startRef, UserAuthenticationRequest.class))
        .filter(isStatusCheckAllowed())
        .map(userAuthenticationRequest -> userAuthenticationRequest.getAuthenticationStartResponse().orderRef())
        .transformDeferred(bankIdClient::collect).flatMap(collectResponse -> {
          String reference = null;
          UserAuthenticationRequest userAuthenticationRequest = getUserAuthenticationRequestCache().get(startRef,
              UserAuthenticationRequest.class);
          if (Objects.isNull(userAuthenticationRequest)) {
            log.error("Request with startRef={} is missing in cache.", startRef);
            return Mono.error(new BankIDException(
                ErrorResponse.builder().errorCode("INVALID_REQUEST").details("Request is invalid!").build()));
          }
          if (COMPLETE.equals(collectResponse.status())) {
            log.info("Completed authentication!");
            userAuthenticationRequest.setEndTime(Instant.now());
            userAuthenticationRequest.setLastStatusCheckTime(Instant.now());

            parseSignature(collectResponse.signature());
            /**
             Base64String xmlSigB64 = new Base64String(collectResponse.getCompletionData().getSignature());

             // Parse the digital signature to retrieve more information.
             DigitalSignature digSig = new DigitalSignature(xmlSigB64);

             String visibleData = null;

             // Visible data may be empty
             if (digSig.getUserVisibleData() != null && !digSig.getUserVisibleData().isEmpty()) {
             visibleData = new String(Base64.getDecoder().decode(digSig.getUserVisibleData()),
             StandardCharsets.UTF_8);
             }
             AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
             .userName(collectResponse.user().name())
             .givenName(collectResponse.user().givenName())
             .surname(collectResponse.user().surname())
             .ssn(collectResponse.user().personalNumber())
             .signedDisplayText()**/
            AuthenticatedUser authenticatedUser = AuthenticatedUser.builder().build();
            auditService.save(authenticatedUser);
            reference = oneTimeReferenceService.getReference(authenticatedUser);
          }
          userAuthenticationRequest.setAuthenticationStatus(collectResponse.status());
          userAuthenticationRequest.setLastStatusCheckTime(Instant.now());
          return Mono.just(StatusResponse.builder().status(collectResponse.status()).reference(reference).build());
        }).switchIfEmpty(getCurrentStatus(startRef));

  }

  private void parseSignature(String signature) {
// Decode the base64 encoded xml signature to a regular XML string.
    String xmlSignature = new String(
        Base64.getDecoder().decode(signature),
        StandardCharsets.UTF_8);

    try {
      log.info("XML response:  {}", xmlSignature);
      /**
       // Parse
       Document doc = parseAsDocument(xmlSignature);
       XPath xPath = XPathFactory.newInstance().newXPath();
       xPath.setNamespaceContext(new BidSignatureNamespaceContext());

       Element docElement = doc.getDocumentElement();

       String bidSignedData = "/digsig:Signature/digsig:Object/bidsig:bankIdSignedData";

       String signatureUsage =
       getXpathString(
       xPath,
       bidSignedData + "/bidsig:clientInfo/bidsig:funcId",
       docElement);

       String userNonVisibleData =
       getXpathString(
       xPath,
       bidSignedData + "/bidsig:usrNonVisibleData",
       docElement);

       String userVisibleData =
       getXpathString(
       xPath,
       bidSignedData + "/bidsig:usrVisibleData",
       docElement);
       **/
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Cache getUserAuthenticationRequestCache() {
    return cacheManager.getCache("user-authentication-request");
  }


  public Mono<CancelResponse> cancel(String startRef) {
    return Mono.fromSupplier(() -> getUserAuthenticationRequestCache().get(startRef, UserAuthenticationRequest.class))
        .filter(isCancelAllowed())
        .map(userAuthenticationRequest -> userAuthenticationRequest.getAuthenticationStartResponse().orderRef())
        .transformDeferred(bankIdClient::cancel).thenReturn(createCancelResponse(startRef));
  }

  private Mono<StatusResponse> getCurrentStatus(String startRef) {
    UserAuthenticationRequest userAuthenticationRequest = getUserAuthenticationRequestCache().get(startRef,
        UserAuthenticationRequest.class);
    if (Objects.isNull(userAuthenticationRequest)) {
      log.error("Request with startRef={} is missing in cache.", startRef);
      return Mono.error(new BankIDException(
          ErrorResponse.builder().errorCode("INVALID_REQUEST").details("Request is invalid!").build()));
    }
    log.error("Request with orderRef={} already in progress with status={}!",
        userAuthenticationRequest.getAuthenticationStartResponse().orderRef(),
        userAuthenticationRequest.getAuthenticationStatus());
    return Mono.just(StatusResponse.builder().status(userAuthenticationRequest.getAuthenticationStatus()).build());
  }

  private Predicate<UserAuthenticationRequest> isStatusCheckAllowed() {
    return userAuthenticationRequest -> userAuthenticationRequest.getAuthenticationStatus().equals(PENDING)
        && userAuthenticationRequest.getStartTime().until(Instant.now(), ChronoUnit.SECONDS) > 1;
  }


  private CancelResponse createCancelResponse(String startRef) {
    UserAuthenticationRequest userAuthenticationRequest = getUserAuthenticationRequestCache().get(startRef,
        UserAuthenticationRequest.class);
    if (Objects.isNull(userAuthenticationRequest)) {
      log.error("Request with startRef={} is missing in cache.", startRef);
      throw new BankIDException(
          ErrorResponse.builder().errorCode("INVALID_REQUEST").details("Request is invalid!").build());
    }
    Instant instant = Instant.now();
    userAuthenticationRequest.setAuthenticationStatus(CANCELLED);
    userAuthenticationRequest.setLastStatusCheckTime(instant);
    userAuthenticationRequest.setEndTime(instant);

    return CancelResponse.builder().orderRef(startRef).message("Completed successfully!").build();
  }

  private Predicate<UserAuthenticationRequest> isCancelAllowed() {
    return userAuthenticationRequest -> PENDING.equals(userAuthenticationRequest.getAuthenticationStatus());
  }
}
