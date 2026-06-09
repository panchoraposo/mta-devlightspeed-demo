package com.acme.fsi.trading.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class LegacyJndiLookup {
  public DataSource lookupExampleDs() {
    try {
      Context ctx = new InitialContext();
      return (DataSource) ctx.lookup("java:jboss/datasources/ExampleDS");
    } catch (Exception e) {
      return null;
    }
  }
}

