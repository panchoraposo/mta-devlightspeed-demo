package com.acme.fsi.payments.service;

import com.acme.fsi.payments.model.PaymentEntity;

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
public class PaymentLedgerService {
  @PersistenceContext(unitName = "fsiPaymentsPU")
  EntityManager em;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void record(String paymentId, PaymentEntity e) {
    if (e == null) {
      return;
    }
    e.setPaymentId(paymentId);
    e.setCreatedAt(new Date());
    if (e.getClearedFlag() == null) {
      e.setClearedFlag(Boolean.FALSE);
    }
    em.persist(e);
  }

  public List<PaymentEntity> findByPaymentIdLegacyCriteria(String paymentId) {
    Session session = (Session) em.getDelegate();
    Criteria c = session.createCriteria(PaymentEntity.class);
    c.add(Restrictions.eq("paymentId", paymentId));
    return c.list();
  }

  public List<PaymentEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<PaymentEntity> q = em.createQuery("select p from PaymentEntity p order by p.createdAt desc", PaymentEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }
}