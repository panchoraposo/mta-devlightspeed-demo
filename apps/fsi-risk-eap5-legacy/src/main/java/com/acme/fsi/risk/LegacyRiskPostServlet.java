package com.acme.fsi.risk;

import com.acme.fsi.risk.xml.LegacyJaxbRiskRequestParser;
import com.acme.fsi.risk.xml.LegacyRiskRequest;

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
 * POST legacy: soporta XML con JAXB (javax.*) y parsing manual.
 */
public class LegacyRiskPostServlet extends HttpServlet {
  private final LegacyJaxbRiskRequestParser parser = new LegacyJaxbRiskRequestParser();

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String contentType = req.getContentType();
    LegacyRiskRequest body = null;
    if (contentType != null && contentType.toLowerCase().contains("xml")) {
      body = parser.parse(req.getInputStream());
    }

    String customerId = body == null ? req.getParameter("customerId") : body.customerId;
    BigDecimal exposure = body == null ? null : body.exposure;
    Calendar asOfCal = parseDateTime(body == null ? req.getParameter("asOf") : body.asOf);

    if (exposure == null) {
      exposure = parseBigDecimal(req.getParameter("exposure"), new BigDecimal("100000.00"));
    }

    LegacyRiskConfig cfg = LegacyRiskConfig.load();
    LegacyRiskService svc = new LegacyRiskService(cfg);
    RiskDecision decision = svc.assess(customerId, exposure, asOfCal);

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
}

