package com.d3softtech.oauth2.gateway.entity;

public enum CardReaderType {
  CLASS1("class1"), CLASS2("class2"), DEFAULT("class1");
  final String cardReader;

  CardReaderType(String cardReader) {
    this.cardReader = cardReader;
  }

  public String getDefaultCardReader() {
    return DEFAULT.cardReader;
  }
}
