package com.acme.fsi.payments.service;

import com.acme.fsi.payments.model.PaymentEntity;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PaymentDataSeeder {

  @Inject
  PaymentLedgerService ledger;

  void onStart(@Observes StartupEvent ev) {
    seed();
  }

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

    List<String> currencies = new ArrayList<String>();
    currencies.add("USD");
    currencies.add("EUR");
    currencies.add("GBP");

    // Dataset determinístico: mismos paymentId en cada entorno limpio
    // (y si reusás Dev Services + strategy=update, se inserta una sola vez).
    for (int i = 1; i <= 50; i++) {
      PaymentEntity e = new PaymentEntity();
      e.setAmount(new BigDecimal("10.00")
          .add(new BigDecimal(i).multiply(new BigDecimal("1.37")))
          .setScale(2, RoundingMode.HALF_UP));
      e.setCurrency(currencies.get(i % currencies.size()));
      e.setClearedFlag(i % 7 == 0 ? Boolean.TRUE : Boolean.FALSE);
      ledger.record(String.format("seed-%04d", i), e);
    }
  }
}