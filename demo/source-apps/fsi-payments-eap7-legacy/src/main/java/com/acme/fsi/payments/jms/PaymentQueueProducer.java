package com.acme.fsi.payments.jms;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@Stateless
public class PaymentQueueProducer {

  @Resource(lookup = "java:/ConnectionFactory")
  ConnectionFactory connectionFactory;

  @Resource(lookup = "java:/jms/queue/Payments")
  Destination paymentsQueue;

  public void send(String payload) {
    Connection conn = null;
    Session session = null;
    try {
      conn = connectionFactory.createConnection();
      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(paymentsQueue);
      TextMessage msg = session.createTextMessage(payload);
      producer.send(msg);
    } catch (Exception e) {
      throw new RuntimeException("Failed to enqueue payment", e);
    } finally {
      try { if (session != null) session.close(); } catch (Exception ignored) {}
      try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }
  }
}