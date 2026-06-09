package com.acme.fsi.payments.service;

import jakarta.inject.Inject;
import javax.sql.DataSource;

public class LegacyJndiPaymentLookup {
  @Inject
  DataSource dataSource;

  public DataSource lookupPaymentsDs() {
    return dataSource;
  }
