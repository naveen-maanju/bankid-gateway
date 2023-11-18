package com.d3softtech.oauth2.gateway.entity.bankid;

import lombok.Builder;

@Builder
public record AuthenticatedUser(String userName, String ssn, String givenName, String surname, String signedDisplayText) {

}
