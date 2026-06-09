package com.acme.fsi.risk;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Random;
import com.acme.fsi.risk.soap.LegacyRiskSoapClient;

public class LegacyRiskService {
  private final LegacyRiskConfig cfg;
  private final LegacyCreditBureauClient creditClient;

  public LegacyRiskService(LegacyRiskConfig cfg) {
    this.cfg = cfg;
    this.creditClient = new LegacyCreditBureauClient(cfg.creditBureauUrl());
  }

  public RiskDecision assess(String customerId, BigDecimal exposure, Calendar asOf) {
    int bureauScore = creditClient.getScore(customerId);
    int riskScore = RiskScoringEngine.computeRiskScore(exposure, asOf, bureauScore);

    // "token" legacy: Random + MD5-ish base64 -> hallazgos de seguridad típicos
    String tokenSeed = customerId + ":" + exposure + ":" + System.currentTimeMillis() + ":" + new Random().nextLong();
    String token = DatatypeConverter.printBase64Binary(LegacyWeakCrypto.md5(tokenSeed + ":" + cfg.salt()));

    // JNDI lookup legacy (intencional): para disparar hallazgos al ir a Quarkus
    new LegacyJndiDataSourceLookup().lookupExampleDs();

    // SOAP client legacy (intencional): para disparar hallazgos típicos (JAX-WS removido del JDK)
    new LegacyRiskSoapClient().notifyDecision(customerId, riskScore);

    return new RiskDecision(customerId, riskScore, asOf, bureauScore, token);
  }
}

