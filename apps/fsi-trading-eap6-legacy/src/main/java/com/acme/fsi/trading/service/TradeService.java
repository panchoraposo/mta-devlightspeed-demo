package com.acme.fsi.trading.service;

import com.acme.fsi.trading.model.Trade;
import com.acme.fsi.trading.soap.LegacySettlementSoapClient;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Stateless
public class TradeService {
  @PersistenceContext(unitName = "fsiTradingPU")
  EntityManager em;

  @PostConstruct
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void seed() {
    if (count() > 0) return;
    Trade t = new Trade();
    t.setSymbol("ACME");
    t.setNotional(new BigDecimal("250000.00"));
    t.setTradeDate(new Date());
    em.persist(t);
  }

  public long count() {
    return em.createQuery("select count(t) from Trade t", Long.class).getSingleResult();
  }

  public List<Trade> list() {
    return em.createQuery("select t from Trade t order by t.id desc", Trade.class).getResultList();
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Trade book(String symbol, BigDecimal notional) {
    Trade t = new Trade();
    t.setSymbol(symbol);
    t.setNotional(notional);
    t.setTradeDate(new Date());
    t.setClearedFlag("N");
    em.persist(t);

    // JNDI lookup legacy (intencional): para disparar hallazgos al ir a Quarkus
    new LegacyJndiLookup().lookupExampleDs();

    new LegacySettlementSoapClient().notifyBooked(String.valueOf(t.getId()), symbol);
    return t;
  }
}

