package com.acme.fsi.cards.service;

import javax.ejb.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AuthStatusStore {
  private final Map<String, String> statuses = new ConcurrentHashMap<String, String>();

  public void setStatus(String authId, String status) {
    if (authId == null) return;
    statuses.put(authId, status == null ? "" : status);
  }

  public String getStatus(String authId) {
    if (authId == null) return null;
    return statuses.get(authId);
  }
}
