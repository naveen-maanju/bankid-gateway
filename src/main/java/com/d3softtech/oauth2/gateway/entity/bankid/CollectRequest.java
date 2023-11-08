package com.d3softtech.oauth2.gateway.entity.bankid;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record CollectRequest(@NonNull String orderRef) {

}
