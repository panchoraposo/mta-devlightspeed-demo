package com.acme.fsi.cards.service;

import com.acme.fsi.cards.model.AuthEntity;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Singleton
@Startup
public class AuthDataSeeder {

  @EJB
  AuthLedgerService ledger;

  @PostConstruct
  public void seed() {
    try {
      List<AuthEntity> existing = ledger.listLatest(1);
      if (existing != null && !existing.isEmpty()) {
        return;
      }
    } catch (Exception ignored) {
      // legacy: ignore
    }

    for (int i = 0; i < 5; i++) {
      AuthEntity e = new AuthEntity();
      e.setMerchantId("MERCH-" + (1000 + i));
      e.setPanLast4(String.format("%04d", 1000 + i));
      e.setAmount(new BigDecimal("20.00").add(new BigDecimal(i * 5)));
      e.setCurrency(i % 2 == 0 ? "USD" : "EUR");
      e.setFlaggedFlag(Boolean.FALSE);
      ledger.record("seed-" + UUID.randomUUID(), e);
    }
  }
}
