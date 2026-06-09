package com.acme.fsi.trading.service;

import com.acme.fsi.trading.model.Trade;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TradeQueryDao {
  @PersistenceContext(unitName = "fsiTradingPU")
  EntityManager em;

  public List<Trade> searchBySymbolUnsafe(String symbol) {
    String s = symbol == null ? "" : symbol.trim();
    String jpql = "select t from Trade t where lower(t.symbol) like '%" + s.toLowerCase() + "%' order by t.id desc";
    return em.createQuery(jpql, Trade.class).getResultList();
  }
}

