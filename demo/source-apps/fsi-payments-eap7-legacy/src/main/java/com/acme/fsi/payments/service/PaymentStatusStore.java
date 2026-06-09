package com.acme.fsi.payments.service;

import jakarta.ejb.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PaymentStatusStore {
  private final Map<String, String> statuses = new ConcurrentHashMap<String, String>();

  public void setStatus(String paymentId, String status) {
    if (paymentId == null) return;
    statuses.put(paymentId, status == null ? "" : status);
  }

  public String getStatus(String paymentId) {
    if (paymentId == null) return null;
    return statuses.get(paymentId);
  }
}