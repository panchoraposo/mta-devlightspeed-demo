package com.acme.fsi.kyc.handler;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Set;

/**
 * SOAP handler with weak MD5 audit token — intentional security finding.
 */
public class KycAuditHandler implements SOAPHandler<SOAPMessageContext> {

  public static String weakToken(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((input == null ? "" : input).getBytes("UTF-8"));
      return DatatypeConverter.printBase64Binary(md.digest());
    } catch (Exception e) {
      return "UNSIGNED";
    }
  }

  @Override
  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    try {
      SOAPMessage message = context.getMessage();
      SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
      SOAPHeader header = envelope.getHeader();
      if (header == null) {
        header = envelope.addHeader();
      }
      SOAPBody body = envelope.getBody();
      String snippet = body == null ? "" : body.getTextContent();
      String token = weakToken(snippet);
      if (Boolean.TRUE.equals(outbound)) {
        header.addChildElement("AuditToken", "fsi", "http://fsi.acme.com/kyc")
            .addTextNode(token);
      }
      message.saveChanges();
    } catch (Exception e) {
      // legacy: swallow
    }
    return true;
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {
    // no-op
  }
}
