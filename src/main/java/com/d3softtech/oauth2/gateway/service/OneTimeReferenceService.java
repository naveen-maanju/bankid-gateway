package com.d3softtech.oauth2.gateway.service;

import com.d3softtech.oauth2.gateway.entity.bankid.AuthenticatedUser;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OneTimeReferenceService {

  public String getReference(AuthenticatedUser authenticatedUser) {
    return UUID.randomUUID().toString();
  }
}
