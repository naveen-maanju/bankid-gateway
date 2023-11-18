package com.d3softtech.oauth2.gateway.entity.bankid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(String errorCode, String details) {

}
