package com.acme.fsi.kyc.soap;

import com.acme.fsi.kyc.handler.KycAuditHandler;
import com.acme.fsi.kyc.model.KycApplication;
import com.acme.fsi.kyc.model.KycDecision;
import com.acme.fsi.kyc.service.KycCaseStore;
import com.acme.fsi.kyc.service.LegacyKycConfig;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import java.util.UUID;

@WebService(
    serviceName = "KycService",
    portName = "KycServicePort",
    endpointInterface = "com.acme.fsi.kyc.soap.KycService",
    targetNamespace = "http://fsi.acme.com/kyc")
@HandlerChain(file = "kyc-handlers.xml")
public class KycServiceImpl implements KycService {

  private final KycCaseStore store = KycCaseStore.getInstance();

  @Override
  public KycDecision submitApplication(KycApplication application) {
    // System property + /etc path config (intencional)
    String scoringMode = LegacyKycConfig.loadScoringMode();

    String applicationId = application != null && application.getApplicationId() != null
        && !application.getApplicationId().trim().isEmpty()
        ? application.getApplicationId().trim()
        : "KYC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    if (application == null) {
      application = new KycApplication();
    }
    application.setApplicationId(applicationId);

    String status = "APPROVED";
    String reason = "auto-" + scoringMode;
    if (application.getNationalId() == null || application.getNationalId().trim().isEmpty()) {
      status = "REJECTED";
      reason = "missing-national-id";
    } else if (application.getFullName() == null || application.getFullName().trim().length() < 3) {
      status = "REVIEW";
      reason = "weak-identity";
    }

    KycDecision decision = new KycDecision();
    decision.setApplicationId(applicationId);
    decision.setStatus(status);
    decision.setReason(reason);
    decision.setAuditToken(KycAuditHandler.weakToken(applicationId + ":" + status));
    store.put(decision);
    return decision;
  }

  @Override
  public KycDecision getStatus(String applicationId) {
    KycDecision existing = store.get(applicationId);
    if (existing != null) {
      return existing;
    }
    KycDecision missing = new KycDecision();
    missing.setApplicationId(applicationId);
    missing.setStatus("NOT_FOUND");
    missing.setReason("unknown");
    return missing;
  }
}
