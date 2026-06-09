package com.acme.fsi.payments.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class LegacyJndiPaymentLookup {
  public DataSource lookupPaymentsDs() {
    try {
      Context ctx = new InitialContext();
      return (DataSource) ctx.lookup("java:jboss/datasources/PaymentsDS");
    } catch (Exception e) {
      return null;
    }
  }
}

