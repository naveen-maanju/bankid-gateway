package com.d3softtech.oauth2.gateway.exception;

import com.d3softtech.oauth2.gateway.entity.bankid.ErrorResponse;

public class BankIDException extends RuntimeException {

  private final ErrorResponse errorResponse;

  public BankIDException(ErrorResponse errorResponse) {
    super(errorResponse.details());
    this.errorResponse = errorResponse;
  }

}
