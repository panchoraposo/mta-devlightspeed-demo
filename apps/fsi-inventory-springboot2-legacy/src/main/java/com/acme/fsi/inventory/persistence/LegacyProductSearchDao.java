package com.acme.fsi.inventory.persistence;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class LegacyProductSearchDao {
  @PersistenceContext
  EntityManager em;

  @SuppressWarnings("unchecked")
  public List<Object[]> searchByNameUnsafe(String q) {
    String term = (q == null) ? "" : q.trim();
    String sql = "select id, sku, name, quantity from products where lower(name) like '%" + term.toLowerCase() + "%'";
    return em.createNativeQuery(sql).getResultList();
  }
}

