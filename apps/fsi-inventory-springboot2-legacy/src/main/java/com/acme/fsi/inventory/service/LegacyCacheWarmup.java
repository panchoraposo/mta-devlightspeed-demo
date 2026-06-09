package com.acme.fsi.inventory.service;

import org.apache.commons.collections.map.LRUMap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class LegacyCacheWarmup {
  private final Map<String, Object> cache = new LRUMap(100);

  @PostConstruct
  public void warm() {
    cache.put("status", "warmed");
  }
}

