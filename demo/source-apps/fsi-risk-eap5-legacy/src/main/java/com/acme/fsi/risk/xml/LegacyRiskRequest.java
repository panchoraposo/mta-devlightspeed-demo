package com.acme.fsi.risk.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "riskRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyRiskRequest {
  @XmlElement
  public String customerId;

  @XmlElement
  public BigDecimal exposure;

  @XmlElement
  public String asOf;
}

