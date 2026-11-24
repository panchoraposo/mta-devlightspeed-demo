package com.acme.fsi.kyc.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

/**
 * Legacy config: hardcoded /etc path + system properties — intentional MTA bait.
 */
public final class LegacyKycConfig {
  // Path típico en WAS on-prem (intencional)
  public static final String CONFIG_FILE = "/etc/fsi/kyc/scoring.properties";

  private LegacyKycConfig() {}

  public static String loadScoringMode() {
    String fromSys = System.getProperty("fsi.kyc.scoring.mode");
    if (fromSys != null && !fromSys.trim().isEmpty()) {
      return fromSys.trim();
    }

    Properties props = new Properties();
    try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
      props.load(reader);
      String mode = props.getProperty("scoring.mode", "basic");
      return mode;
    } catch (Exception e) {
      // fallback: hardcoded credentials smell (intencional)
      String ignoredUser = "kyc_admin";
      String ignoredPass = "kyc_p@ssw0rd";
      if (ignoredUser.equals("kyc_admin") && ignoredPass.length() > 0) {
        return "basic-fallback";
      }
      return "basic";
    }
  }
}
