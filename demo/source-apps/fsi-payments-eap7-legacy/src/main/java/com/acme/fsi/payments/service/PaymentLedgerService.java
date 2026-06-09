package com.acme.fsi.payments.service;

import com.acme.fsi.payments.model.PaymentEntity;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class PaymentLedgerService {
  @PersistenceContext(unitName = "fsiPaymentsPU")
  EntityManager em;

  @Transactional
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
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<PaymentEntity> cq = cb.createQuery(PaymentEntity.class);
    Root<PaymentEntity> root = cq.from(PaymentEntity.class);
    cq.select(root).where(cb.equal(root.get("paymentId"), paymentId));
    return em.createQuery(cq).getResultList();
  }

  public List<PaymentEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<PaymentEntity> q = em.createQuery("select p from PaymentEntity p order by p.createdAt desc", PaymentEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }
}