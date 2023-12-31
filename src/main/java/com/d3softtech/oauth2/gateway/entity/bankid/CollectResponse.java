package com.d3softtech.oauth2.gateway.entity.bankid;

import lombok.Builder;

@Builder
public record CollectResponse(String orderRef, CollectStatus status, String hintCode, User user, String bankIdIssueDate,
                              String signature, String ocspResponse) {

}
