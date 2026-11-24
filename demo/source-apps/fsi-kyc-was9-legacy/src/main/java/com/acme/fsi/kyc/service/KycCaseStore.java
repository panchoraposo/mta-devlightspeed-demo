package com.acme.fsi.kyc.service;

import com.acme.fsi.kyc.model.KycDecision;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory store (no JPA) — keeps SOAP demo simple for Liberty. */
public final class KycCaseStore {
  private static final KycCaseStore INSTANCE = new KycCaseStore();
  private final Map<String, KycDecision> cases = new ConcurrentHashMap<String, KycDecision>();

  private KycCaseStore() {}

  public static KycCaseStore getInstance() {
    return INSTANCE;
  }

  public void put(KycDecision decision) {
    if (decision == null || decision.getApplicationId() == null) return;
    cases.put(decision.getApplicationId(), decision);
  }

  public KycDecision get(String applicationId) {
    if (applicationId == null) return null;
    return cases.get(applicationId);
  }
}
