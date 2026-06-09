package com.acme.fsi.inventory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SupplierCatalogClient {
  private final RestTemplate restTemplate;
  private final String supplierUrl;

  public SupplierCatalogClient(RestTemplate restTemplate, @Value("${inventory.supplier.url:https://example.invalid/api/v1/catalog}") String supplierUrl) {
    this.restTemplate = restTemplate;
    this.supplierUrl = supplierUrl;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> fetchCatalog(String sku) {
    String url = supplierUrl + "?sku=" + sku;
    return restTemplate.getForObject(url, Map.class);
  }
}

