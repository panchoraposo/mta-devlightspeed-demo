package com.acme.fsi.payments.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "payments")
public class PaymentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, length = 64)
  private String paymentId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private Date createdAt;

  @Type(type = "yes_no")
  @Column(length = 1)
  private Boolean clearedFlag;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getClearedFlag() {
    return clearedFlag;
  }

  public void setClearedFlag(Boolean clearedFlag) {
    this.clearedFlag = clearedFlag;
  }
}