package com.acme.fsi.payments.service;

import com.acme.fsi.payments.model.PaymentEntity;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Singleton
@Startup
public class PaymentDataSeeder {

  @EJB
  PaymentLedgerService ledger;

  @PostConstruct
  public void seed() {
    try {
      List<PaymentEntity> existing = ledger.listLatest(1);
      if (existing != null && !existing.isEmpty()) {
        return;
      }
    } catch (Exception ignored) {
      // legacy: ignore
    }

    for (int i = 0; i < 5; i++) {
      PaymentEntity e = new PaymentEntity();
      e.setAmount(new BigDecimal("10.00").add(new BigDecimal(i)));
      e.setCurrency(i % 2 == 0 ? "USD" : "EUR");
      e.setClearedFlag(Boolean.FALSE);
      ledger.record("seed-" + UUID.randomUUID(), e);
    }
  }
}

