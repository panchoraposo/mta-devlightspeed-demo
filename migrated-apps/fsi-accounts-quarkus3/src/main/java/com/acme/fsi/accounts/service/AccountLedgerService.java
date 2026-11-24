package com.acme.fsi.accounts.service;

import com.acme.fsi.accounts.model.AccountEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class AccountLedgerService {
  @Inject
  EntityManager em;

  @Transactional
  public void open(String accountId, AccountEntity e) {
    if (e == null) {
      return;
    }
    e.setAccountId(accountId);
    e.setOpenedAt(new Date());
    if (e.getActiveFlag() == null) {
      e.setActiveFlag(Boolean.TRUE);
    }
    em.persist(e);
  }

  public List<AccountEntity> findByAccountId(String accountId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AccountEntity> cq = cb.createQuery(AccountEntity.class);
    Root<AccountEntity> root = cq.from(AccountEntity.class);
    cq.select(root).where(cb.equal(root.get("accountId"), accountId));
    return em.createQuery(cq).getResultList();
  }

  public List<AccountEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<AccountEntity> q = em.createQuery(
        "select a from AccountEntity a order by a.openedAt desc", AccountEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }
}
