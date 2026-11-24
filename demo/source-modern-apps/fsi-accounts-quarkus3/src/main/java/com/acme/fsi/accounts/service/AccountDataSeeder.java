package com.acme.fsi.accounts.service;

import com.acme.fsi.accounts.model.AccountEntity;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class AccountDataSeeder {

  @Inject
  AccountLedgerService ledger;

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    List<AccountEntity> existing = ledger.listLatest(1);
    if (existing != null && !existing.isEmpty()) {
      return;
    }
    String[] customers = {"CUST-1001", "CUST-1002", "CUST-1003", "CUST-1004", "CUST-1005"};
    String[] types = {"CHECKING", "SAVINGS", "CHECKING", "MONEY_MARKET", "SAVINGS"};
    for (int i = 0; i < customers.length; i++) {
      AccountEntity e = new AccountEntity();
      e.setCustomerId(customers[i]);
      e.setAccountType(types[i]);
      e.setCurrency(i % 2 == 0 ? "USD" : "EUR");
      e.setBalance(new BigDecimal("1000.00").add(new BigDecimal(i * 250)));
      e.setActiveFlag(Boolean.TRUE);
      ledger.open("ACC-SEED-" + (i + 1), e);
    }
  }
}
