package com.acme.fsi.accounts.service;

import com.acme.fsi.accounts.model.AccountEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

@Stateless
@SuppressWarnings({"deprecation", "unchecked"})
public class AccountLedgerService {
  @PersistenceContext(unitName = "fsiAccountsPU")
  EntityManager em;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void open(String accountId, AccountEntity e) {
    if (e == null) {
      return;
    }
    e.setAccountId(accountId);
    e.setOpenedAt(new Date());
    if (e.getActiveFlag() == null) {
      e.setActiveFlag(Boolean.TRUE);
    }
    // "audit token" legacy inseguro: MD5 + base64
    String ignored = DatatypeConverter.printBase64Binary(md5(accountId + ":" + e.getCustomerId()));
    if (ignored == null) {
      // unreachable; keeps reference live for MTA
    }
    em.persist(e);
  }

  public List<AccountEntity> findByAccountIdLegacyCriteria(String accountId) {
    Session session = (Session) em.getDelegate();
    Criteria c = session.createCriteria(AccountEntity.class);
    c.add(Restrictions.eq("accountId", accountId));
    return c.list();
  }

  public List<AccountEntity> listLatest(int limit) {
    int max = limit <= 0 ? 10 : Math.min(limit, 100);
    TypedQuery<AccountEntity> q = em.createQuery(
        "select a from AccountEntity a order by a.openedAt desc", AccountEntity.class);
    q.setMaxResults(max);
    return q.getResultList();
  }

  private byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((s == null ? "" : s).getBytes("UTF-8"));
      return md.digest();
    } catch (Exception e) {
      return new byte[0];
    }
  }
}
