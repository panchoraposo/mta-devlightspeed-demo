package com.acme.fsi.risk;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * App "EAP 5-era": Servlet 2.5, parsing legacy con DatatypeConverter (JAXB),
 * y salida manual (sin JSON libs).
 */
public class LegacyRiskServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String customerId = req.getParameter("customerId");
    String exposureStr = req.getParameter("exposure");
    String asOf = req.getParameter("asOf"); // ISO-8601 datetime
    String format = req.getParameter("format"); // json|xml

    // config legacy (filesystem + system properties), típico en apps pre-cloud
    LegacyRiskConfig cfg = LegacyRiskConfig.load();

    if (format == null || format.trim().length() == 0) {
      format = "json";
    }

    BigDecimal exposure = parseBigDecimal(exposureStr, new BigDecimal("100000.00"));
    Calendar asOfCal = parseDateTime(asOf);

    LegacyRiskService svc = new LegacyRiskService(cfg);
    RiskDecision decision = svc.assess(customerId, exposure, asOfCal);

    if ("xml".equalsIgnoreCase(format)) {
      resp.setContentType("application/xml");
      PrintWriter out = resp.getWriter();
      out.println(decision.toLegacyXml());
      out.flush();
      return;
    }

    resp.setContentType("application/json");
    PrintWriter out = resp.getWriter();
    out.print(decision.toLegacyJson());
    out.flush();
  }

  private Calendar parseDateTime(String iso) {
    if (iso == null || iso.trim().length() == 0) {
      return null;
    }
    try {
      return DatatypeConverter.parseDateTime(iso);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private BigDecimal parseBigDecimal(String s, BigDecimal fallback) {
    if (s == null || s.trim().length() == 0) {
      return fallback;
    }
    try {
      return new BigDecimal(s.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}