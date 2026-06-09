package com.acme.fsi.risk;

import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

/**
 * Auth "legacy" intencionalmente simple: token en query param y sesión.
 * Sirve para generar hallazgos típicos (auth, crypto débil, sesión, etc.).
 */
public class LegacyAuthFilter implements Filter {
  private static final Logger log = Logger.getLogger(LegacyAuthFilter.class);

  public void init(FilterConfig filterConfig) {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI();
    if (path != null && (path.endsWith("/health") || path.endsWith("/risk"))) {
      chain.doFilter(request, response);
      return;
    }

    HttpSession session = req.getSession(true);
    Object authed = session.getAttribute("AUTH_OK");
    if (Boolean.TRUE.equals(authed)) {
      chain.doFilter(request, response);
      return;
    }

    String user = req.getParameter("user");
    String token = req.getParameter("token");

    LegacyRiskConfig cfg = LegacyRiskConfig.load();
    String expected = DatatypeConverter.printBase64Binary(
        LegacyWeakCrypto.md5((user == null ? "" : user) + ":" + cfg.salt())
    );

    if (token != null && token.equals(expected)) {
      session.setAttribute("AUTH_OK", Boolean.TRUE);
      log.info("Legacy auth succeeded for user=" + user);
    } else {
      log.warn("Legacy auth failed for user=" + user);
    }

    chain.doFilter(request, response);
  }

  public void destroy() {}
}

