package com.acme.fsi.accounts.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Manual JNDI lookup (WAS/Liberty style) — intentional MTA bait for Quarkus.
 */
public class LegacyJndiAccountsLookup {
  public DataSource lookupAccountsDs() {
    try {
      Context ctx = new InitialContext();
      // WAS Traditional / Liberty binding name
      return (DataSource) ctx.lookup("jdbc/AccountsDS");
    } catch (Exception e) {
      return null;
    }
  }
}
