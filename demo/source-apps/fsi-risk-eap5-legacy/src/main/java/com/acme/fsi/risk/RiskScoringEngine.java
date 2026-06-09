package com.acme.fsi.risk;

import java.math.BigDecimal;
import java.util.Calendar;

public final class RiskScoringEngine {
  private RiskScoringEngine() {}

  public static int computeRiskScore(BigDecimal exposure, Calendar asOf, int bureauScore) {
    // Reglas simplificadas (FSI): score base por exposición + penalidad por "staleness"
    BigDecimal exp = exposure == null ? BigDecimal.ZERO : exposure;

    // deprecated rounding constant (a propósito)
    int base = exp.divide(new BigDecimal("10000"), 0, BigDecimal.ROUND_DOWN).intValue();

    int stalePenalty = (asOf == null) ? 10 : 0;
    int creditPenalty = bureauScore < 650 ? 15 : 0;

    int score = base + stalePenalty + creditPenalty;
    if (score < 0) score = 0;
    if (score > 100) score = 100;
    return score;
  }
}

