package com.acme.fsi.risk;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;

public class RiskDecision {
  private final String customerId;
  private final int score;
  private final Calendar asOf;
  private final int bureauScore;
  private final String token;

  public RiskDecision(String customerId, int score, Calendar asOf, int bureauScore, String token) {
    this.customerId = customerId;
    this.score = score;
    this.asOf = asOf;
    this.bureauScore = bureauScore;
    this.token = token;
  }

  public String toLegacyJson() {
    String asOfStr = asOf == null ? "" : DatatypeConverter.printDateTime(asOf);
    return "{"
        + "\"customerId\":\"" + escape(customerId) + "\","
        + "\"score\":" + score + ","
        + "\"bureauScore\":" + bureauScore + ","
        + "\"asOf\":\"" + escape(asOfStr) + "\","
        + "\"token\":\"" + escape(token) + "\""
        + "}";
  }

  public String toLegacyXml() {
    String asOfStr = asOf == null ? "" : DatatypeConverter.printDateTime(asOf);
    return "<risk>\n"
        + "  <customerId>" + escape(customerId) + "</customerId>\n"
        + "  <score>" + score + "</score>\n"
        + "  <bureauScore>" + bureauScore + "</bureauScore>\n"
        + "  <asOf>" + escape(asOfStr) + "</asOf>\n"
        + "  <token>" + escape(token) + "</token>\n"
        + "</risk>";
  }

  private String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}

