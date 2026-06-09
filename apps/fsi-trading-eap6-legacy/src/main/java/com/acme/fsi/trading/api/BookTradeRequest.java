package com.acme.fsi.trading.api;

import java.math.BigDecimal;

public class BookTradeRequest {
  private String symbol;
  private BigDecimal notional;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public BigDecimal getNotional() {
    return notional;
  }

  public void setNotional(BigDecimal notional) {
    this.notional = notional;
  }
}

