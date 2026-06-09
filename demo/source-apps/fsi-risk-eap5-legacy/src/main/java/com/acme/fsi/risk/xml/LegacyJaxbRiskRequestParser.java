package com.acme.fsi.risk.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

public class LegacyJaxbRiskRequestParser {
  public LegacyRiskRequest parse(InputStream in) {
    try {
      JAXBContext ctx = JAXBContext.newInstance(LegacyRiskRequest.class);
      Unmarshaller u = ctx.createUnmarshaller();
      return (LegacyRiskRequest) u.unmarshal(in);
    } catch (Exception e) {
      return null;
    }
  }
}

