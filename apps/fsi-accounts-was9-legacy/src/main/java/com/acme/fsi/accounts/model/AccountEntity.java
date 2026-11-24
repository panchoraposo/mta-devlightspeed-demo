package com.acme.fsi.accounts.model;

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
@Table(name = "accounts")
public class AccountEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, length = 64)
  private String accountId;

  @Column(nullable = false, length = 64)
  private String customerId;

  @Column(nullable = false, length = 32)
  private String accountType;

  @Column(nullable = false)
  private BigDecimal balance;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private Date openedAt;

  // Hibernate 5 yes_no — intentional bait for Quarkus / Hibernate 6
  @Type(type = "yes_no")
  @Column(length = 1)
  private Boolean activeFlag;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Date getOpenedAt() {
    return openedAt;
  }

  public void setOpenedAt(Date openedAt) {
    this.openedAt = openedAt;
  }

  public Boolean getActiveFlag() {
    return activeFlag;
  }

  public void setActiveFlag(Boolean activeFlag) {
    this.activeFlag = activeFlag;
  }
}
