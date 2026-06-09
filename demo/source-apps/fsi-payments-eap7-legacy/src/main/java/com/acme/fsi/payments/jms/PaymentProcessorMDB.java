package com.acme.fsi.payments.jms;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.xml.bind.DatatypeConverter;
import java.io.StringReader;
import java.security.MessageDigest;

import com.acme.fsi.payments.service.PaymentStatusStore;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/Payments"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class PaymentProcessorMDB implements MessageListener {

  @Inject
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
       else {
        store.setStatus(paymentId, "FAILED");
      }
     catch (Exception e) {
      // legacy: swallow exception
    }
  }

  private byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((s == null ? "" : s).getBytes("UTF-8"));
      return md.digest();
     catch (Exception e) {
      return new byte[0];
    }
  }

