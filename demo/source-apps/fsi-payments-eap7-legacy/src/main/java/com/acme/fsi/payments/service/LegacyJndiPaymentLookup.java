package com.acme.fsi.payments.service;

import javax.sql.DataSource;

public class LegacyJndiPaymentLookup {
  public DataSource lookupPaymentsDs() {
    try {
      javax.naming.Context ctx = new javax.naming.InitialContext();
      return (DataSource) ctx.lookup("java:jboss/datasources/PaymentsDS");
    } catch (Exception e) {
      return null;
    }
  }
}
