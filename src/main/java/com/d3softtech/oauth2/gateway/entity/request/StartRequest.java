package com.d3softtech.oauth2.gateway.entity.request;

public record StartRequest(String userVisibleData, String userNonVisibleData,
                           String userVisibleDataFormat, boolean allowFingerPrint) {

}
