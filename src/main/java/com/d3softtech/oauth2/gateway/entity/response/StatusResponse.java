package com.d3softtech.oauth2.gateway.entity.response;

import com.d3softtech.oauth2.gateway.entity.bankid.CollectStatus;
import lombok.Builder;

@Builder
public record StatusResponse(CollectStatus status) {

}
