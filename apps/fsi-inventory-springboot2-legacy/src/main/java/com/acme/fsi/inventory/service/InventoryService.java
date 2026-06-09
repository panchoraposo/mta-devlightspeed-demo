package com.acme.fsi.inventory.service;

import com.acme.fsi.inventory.api.ProductDto;
import com.acme.fsi.inventory.persistence.Product;
import com.acme.fsi.inventory.persistence.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
  private final ProductRepository repo;

  public InventoryService(ProductRepository repo) {
    this.repo = repo;
  }

  public List<Product> list() {
    return repo.findAll();
  }

  public Optional<Product> getBySku(String sku) {
    return repo.findBySku(sku);
  }

  @Transactional
  public Product upsert(ProductDto dto) {
    Product p = repo.findBySku(dto.getSku()).orElseGet(Product::new);
    p.setSku(dto.getSku());
    p.setName(dto.getName());
    p.setQuantity(dto.getQuantity());
    p.setUpdatedAt(Instant.now());
    return repo.save(p);
  }
}

