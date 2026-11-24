package com.acme.fsi.kyc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "decision", namespace = "http://fsi.acme.com/kyc")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KycDecision", propOrder = {"applicationId", "status", "reason", "auditToken"})
public class KycDecision {

  @XmlElement
  private String applicationId;

  @XmlElement
  private String status;

  @XmlElement
  private String reason;

  @XmlElement
  private String auditToken;

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getAuditToken() {
    return auditToken;
  }

  public void setAuditToken(String auditToken) {
    this.auditToken = auditToken;
  }
}
