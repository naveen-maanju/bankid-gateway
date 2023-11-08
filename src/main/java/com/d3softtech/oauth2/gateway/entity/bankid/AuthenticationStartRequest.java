package com.d3softtech.oauth2.gateway.entity.bankid;

import com.d3softtech.oauth2.gateway.properties.AuthenticationRequirements;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Jacksonized
public record AuthenticationStartRequest(String endUserIp, AuthenticationRequirements requirement,
                                         String userVisibleData, String userNonVisibleData,
                                         String userVisibleDataFormat) {

}
