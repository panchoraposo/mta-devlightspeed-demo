package com.acme.fsi.integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;

public class LegacyTransformProcessor implements Processor {
  public void process(Exchange exchange) throws Exception {
    String body = exchange.getIn().getBody(String.class);
    Calendar now = Calendar.getInstance();
    String ts = DatatypeConverter.printDateTime(now);
    exchange.getIn().setHeader("X-Legacy-Timestamp", ts);
    exchange.getIn().setBody("{\"legacy\":true,\"ts\":\"" + ts + "\",\"payload\":" + (body == null ? "\"\"" : body) + "}");
  }
}

