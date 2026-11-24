package com.acme.fsi.cards.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

@Stateless
public class FraudQueueProducer {

  @Resource(lookup = "jms/FraudConnectionFactory")
  ConnectionFactory connectionFactory;

  @Resource(lookup = "jms/FraudQueue")
  Destination fraudQueue;

  public void send(String payload) {
    Connection conn = null;
    Session session = null;
    try {
      conn = connectionFactory.createConnection();
      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(fraudQueue);
      TextMessage msg = session.createTextMessage(payload);
      producer.send(msg);
    } catch (Exception e) {
      throw new RuntimeException("Failed to enqueue fraud check", e);
    } finally {
      try { if (session != null) session.close(); } catch (Exception ignored) {}
      try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }
  }
}
