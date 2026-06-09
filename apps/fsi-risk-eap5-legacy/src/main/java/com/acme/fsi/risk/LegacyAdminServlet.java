package com.acme.fsi.risk;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Endpoint "admin" legacy: dumping de config/system properties (intencionalmente riesgoso).
 */
public class LegacyAdminServlet extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/plain");
    PrintWriter out = resp.getWriter();

    out.println("creditBureau.url=" + LegacyRiskConfig.load().creditBureauUrl());
    out.println("risk.salt=" + LegacyRiskConfig.load().salt());
    out.println();

    out.println("# System properties");
    Enumeration<?> names = System.getProperties().propertyNames();
    while (names.hasMoreElements()) {
      Object k = names.nextElement();
      out.println(k + "=" + System.getProperty(String.valueOf(k)));
    }
    out.flush();
  }
}

