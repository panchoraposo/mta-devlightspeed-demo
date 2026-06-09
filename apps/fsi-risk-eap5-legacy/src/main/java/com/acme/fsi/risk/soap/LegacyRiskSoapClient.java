package com.acme.fsi.risk.soap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * SOAP client "legacy" (JAX-WS / javax.*) para generar hallazgos típicos de modernización.
 * No es funcional: el objetivo es que aparezca en análisis estático.
 */
public class LegacyRiskSoapClient {
  public void notifyDecision(String customerId, int score) {
    try {
      URL wsdl = new URL("http://localhost:8080/risk-notify?wsdl");
      QName serviceName = new QName("http://acme.com/fsi/risk", "RiskNotificationService");
      Service svc = Service.create(wsdl, serviceName);
      svc.getPorts();
    } catch (Exception ignored) {
      // legacy: ignorar y continuar
    }
  }
}

