package com.acme.fsi.trading.service;

import com.acme.fsi.trading.model.Trade;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Iterator;
import java.util.List;

@Stateless
@SuppressWarnings({"deprecation", "unchecked"})
public class LegacyHibernateCriteriaDao {
  @PersistenceContext(unitName = "fsiTradingPU")
  EntityManager em;

  public List<Trade> findBySymbolLegacyCriteria(String symbol) {
    Session session = (Session) em.getDelegate();
    Criteria c = session.createCriteria(Trade.class);
    c.add(Restrictions.ilike("symbol", "%" + (symbol == null ? "" : symbol) + "%"));
    return c.list();
  }

  public int iterateLegacy(String symbol) {
    Session session = (Session) em.getDelegate();
    org.hibernate.Query q = session.createQuery("select t from Trade t where t.symbol = :s");
    q.setParameter("s", symbol);
    Iterator it = q.iterate();
    int n = 0;
    while (it.hasNext()) {
      it.next();
      n++;
    }
    return n;
  }
}

