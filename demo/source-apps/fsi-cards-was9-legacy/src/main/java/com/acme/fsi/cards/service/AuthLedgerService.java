package com.acme.fsi.cards.service;

import com.acme.fsi.cards.model.AuthEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Stateless
@SuppressWarnings({"deprecation", "unchecked"})
public class AuthLedgerService {
  @PersistenceContext(unitName = "fsiCardsPU")
  EntityManager em;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void record(String authId, AuthEntity e) {
    if (e == null) {
      return;
    }
    e.setAuthId(authId);
    e.setCreatedAt(new Date());
    if (e.getFlaggedFlag() == null) {
      e.setFlaggedFlag(Boolean.FALSE);
    }
    em.persist(e);
  }

  public List<AuthEntity> findByAuthIdLegacyCriteria(String authId) {
    Session session = (Session) em.getDelegate();
    Criteria c = session.createCriteria(AuthEntity.class);
    c.add(Restrictions.eq("authId", authId));
    return c.list();
  }

  public List<AuthEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<AuthEntity> q = em.createQuery(
        "select a from AuthEntity a order by a.createdAt desc", AuthEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }
}
