package com.acme.fsi.cards.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/** Manual JNDI lookup (WAS/Liberty style) — intentional MTA bait. */
public class LegacyJndiCardsLookup {
  public DataSource lookupCardsDs() {
    try {
      Context ctx = new InitialContext();
      return (DataSource) ctx.lookup("jdbc/CardsDS");
    } catch (Exception e) {
      return null;
    }
  }
}
