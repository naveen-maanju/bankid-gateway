package com.d3softtech.oauth2.gateway.entity.bankid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthenticationStartResponse(String orderRef, String autoStartToken, String qrStartToken,
                                          String qrStartSecret) implements Serializable {

}
