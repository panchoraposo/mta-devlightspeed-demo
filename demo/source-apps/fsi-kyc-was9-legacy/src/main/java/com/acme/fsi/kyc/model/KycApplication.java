package com.acme.fsi.kyc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "application", namespace = "http://fsi.acme.com/kyc")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KycApplication", propOrder = {"applicationId", "fullName", "nationalId", "country", "riskScore"})
public class KycApplication {

  @XmlElement
  private String applicationId;

  @XmlElement(required = true)
  private String fullName;

  @XmlElement(required = true)
  private String nationalId;

  @XmlElement
  private String country;

  @XmlElement
  private Integer riskScore;

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getNationalId() {
    return nationalId;
  }

  public void setNationalId(String nationalId) {
    this.nationalId = nationalId;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Integer getRiskScore() {
    return riskScore;
  }

  public void setRiskScore(Integer riskScore) {
    this.riskScore = riskScore;
  }
}
