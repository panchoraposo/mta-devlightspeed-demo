package com.acme.fsi.accounts.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;

/**
 * Legacy class name kept for demo continuity. Quarkus injects the datasource
 * instead of doing an InitialContext lookup.
 */
@ApplicationScoped
public class LegacyJndiAccountsLookup {
  @Inject
  DataSource dataSource;

  public DataSource lookupAccountsDs() {
    return dataSource;
  }
}
