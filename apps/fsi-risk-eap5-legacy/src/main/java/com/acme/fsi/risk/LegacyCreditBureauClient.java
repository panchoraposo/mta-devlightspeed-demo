package com.acme.fsi.risk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Cliente HTTP "legacy": URLConnection, endpoints hardcodeados (localhost),
 * sin timeouts consistentes, y parsing manual.
 */
public class LegacyCreditBureauClient {
  private final String baseUrl;

  public LegacyCreditBureauClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public int getScore(String customerId) {
    String cid = (customerId == null || customerId.trim().length() == 0) ? "UNKNOWN" : customerId.trim();
    HttpURLConnection conn = null;
    try {
      URL url = new URL(baseUrl + "?customerId=" + cid);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(500);
      conn.setReadTimeout(500);

      int code = conn.getResponseCode();
      if (code < 200 || code >= 300) {
        return 600; // fallback legacy
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      String line = br.readLine();
      br.close();
      // respuesta esperada: {"score":720}
      if (line == null) return 600;
      int idx = line.indexOf("score");
      if (idx < 0) return 600;
      String digits = line.replaceAll("[^0-9]", "");
      if (digits.length() == 0) return 600;
      return Integer.parseInt(digits);
    } catch (Exception ignored) {
      return 600;
    } finally {
      try { if (conn != null) conn.disconnect(); } catch (Exception ignored) {}
    }
  }
}

