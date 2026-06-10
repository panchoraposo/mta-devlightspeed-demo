package com.acme.fsi.payments.jms;

import com.acme.fsi.payments.service.PaymentStatusStore;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.DatatypeConverter;
import java.io.StringReader;
import java.security.MessageDigest;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/Payments"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class PaymentProcessorMDB implements MessageListener {

  @EJB
  PaymentStatusStore store;

  public void onMessage(Message message) {
    try {
      if (!(message instanceof TextMessage)) return;
      String payload = ((TextMessage) message).getText();

      JsonReader reader = Json.createReader(new StringReader(payload));
      JsonObject obj = reader.readObject();
      String paymentId = obj.getString("paymentId", "");

      // "firma" legacy insegura: MD5 + base64
      String signature = DatatypeConverter.printBase64Binary(md5(payload));
      if (signature.length() > 0) {
        store.setStatus(paymentId, "PROCESSED");
      } else {
        store.setStatus(paymentId, "FAILED");
      }
    } catch (Exception e) {
      // legacy: swallow exception
    }
  }

  private byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((s == null ? "" : s).getBytes("UTF-8"));
      return md.digest();
    } catch (Exception e) {
      return new byte[0];
    }
  }
}