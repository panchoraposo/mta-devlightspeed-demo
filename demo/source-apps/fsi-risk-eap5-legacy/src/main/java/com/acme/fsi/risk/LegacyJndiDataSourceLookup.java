package com.acme.fsi.risk;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * JNDI lookup legacy (intencional) para disparar issues al migrar a Quarkus.
 */
public class LegacyJndiDataSourceLookup {
  public DataSource lookupExampleDs() {
    try {
      Context ctx = new InitialContext();
      return (DataSource) ctx.lookup("java:jboss/datasources/ExampleDS");
    } catch (Exception e) {
      return null;
    }
  }
}

