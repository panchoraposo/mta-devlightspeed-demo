package com.acme.fsi.trading.soap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * SOAP client "legacy" (JAX-WS / javax.*) para generar hallazgos típicos de modernización.
 * No es funcional: el objetivo es que aparezca en análisis estático.
 */
public class LegacySettlementSoapClient {
  public void notifyBooked(String tradeId, String symbol) {
    try {
      URL wsdl = new URL("http://localhost:8080/settlement?wsdl");
      QName serviceName = new QName("http://acme.com/fsi/settlement", "SettlementService");
      Service svc = Service.create(wsdl, serviceName);
      // endpoint stub: en apps reales se obtendría el port y se invocaría el método.
      svc.getPorts();
    } catch (Exception ignored) {
      // legacy: ignorar y continuar
    }
  }
}

