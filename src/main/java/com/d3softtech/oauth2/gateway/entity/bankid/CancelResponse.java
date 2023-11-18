package com.d3softtech.oauth2.gateway.entity.bankid;

import lombok.Builder;

@Builder
public record CancelResponse(String orderRef, String message) {

}
