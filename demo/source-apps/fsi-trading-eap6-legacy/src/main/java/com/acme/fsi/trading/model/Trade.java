package com.acme.fsi.trading.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Type;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "trades")
@XmlRootElement
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tradeGenerator")
  @SequenceGenerator(name = "tradeGenerator", sequenceName = "trades_seq")
  private Long id;

  @Column(nullable = false)
  private String symbol;

  @Column(nullable = false)
  private BigDecimal notional;

  @Column(nullable = false)
  private Date tradeDate;

  @Column(length = 32)
  @Type(type = "yes_no")
  private String clearedFlag;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public BigDecimal getNotional() {
    return notional;
  }

  public void setNotional(BigDecimal notional) {
    this.notional = notional;
  }

  public Date getTradeDate() {
    return tradeDate;
  }

  public void setTradeDate(Date tradeDate) {
    this.tradeDate = tradeDate;
  }

  public String getClearedFlag() {
    return clearedFlag;
  }

  public void setClearedFlag(String clearedFlag) {
    this.clearedFlag = clearedFlag;
  }
}