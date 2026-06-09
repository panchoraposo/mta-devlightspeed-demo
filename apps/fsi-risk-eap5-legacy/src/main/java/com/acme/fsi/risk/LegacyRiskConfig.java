package com.acme.fsi.risk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Config "legacy": primero intenta leer de filesystem (pet-deploy),
 * y si no existe, cae a system properties.
 */
public class LegacyRiskConfig {
  private final Properties props;

  private LegacyRiskConfig(Properties props) {
    this.props = props;
  }

  public static LegacyRiskConfig load() {
    Properties p = new Properties();
    File f = new File("/etc/fsi/risk.properties");
    if (f.exists() && f.isFile()) {
      InputStream in = null;
      try {
        in = new FileInputStream(f);
        p.load(in);
      } catch (Exception ignored) {
        // legacy: ignore y seguir
      } finally {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
      }
    }

    // fallback legacy: system properties (no externalized config)
    copyIfPresent(p, "creditBureau.url");
    copyIfPresent(p, "risk.salt");

    return new LegacyRiskConfig(p);
  }

  private static void copyIfPresent(Properties p, String key) {
    String v = System.getProperty(key);
    if (v != null && v.trim().length() > 0) {
      p.setProperty(key, v.trim());
    }
  }

  public String creditBureauUrl() {
    String v = props.getProperty("creditBureau.url");
    return (v == null || v.trim().length() == 0) ? "http://localhost:8080/credit-bureau/api/v1/score" : v.trim();
  }

  public String salt() {
    String v = props.getProperty("risk.salt");
    return (v == null || v.trim().length() == 0) ? "default-salt" : v.trim();
  }
}

