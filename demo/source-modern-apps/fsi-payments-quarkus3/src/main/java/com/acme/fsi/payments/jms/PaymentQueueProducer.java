package com.acme.fsi.payments.jms;

import com.acme.fsi.payments.service.PaymentStatusStore;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentQueueProducer {

  @Inject
  Vertx vertx;

  @Inject
  PaymentStatusStore store;

  public void send(String payload) {
    // En EAP7 esto era JMS+MDB. En Quarkus lo simulamos con un timer para mantener el flujo demo
    // sin depender de un broker/RA en dev.
    final String paymentId = extractPaymentId(payload);
    if (paymentId == null) {
      return;
    }
    vertx.setTimer(600, t -> store.setStatus(paymentId, "PROCESSED"));
  }

  private String extractPaymentId(String payload) {
    if (payload == null) return null;
    int k = payload.indexOf("\"paymentId\"");
    if (k < 0) return null;
    int colon = payload.indexOf(':', k);
    if (colon < 0) return null;
    int q1 = payload.indexOf('"', colon + 1);
    if (q1 < 0) return null;
    int q2 = payload.indexOf('"', q1 + 1);
    if (q2 < 0) return null;
    return payload.substring(q1 + 1, q2);
  }
}