package com.acme.fsi.inventory.api;

import com.acme.fsi.inventory.persistence.LegacyProductSearchDao;
import com.acme.fsi.inventory.persistence.Product;
import com.acme.fsi.inventory.service.InventoryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
public class InventoryController {
  private final InventoryService service;
  private final LegacyProductSearchDao legacySearch;

  public InventoryController(InventoryService service, LegacyProductSearchDao legacySearch) {
    this.service = service;
    this.legacySearch = legacySearch;
  }

  @GetMapping("/products")
  public List<Product> list() {
    return service.list();
  }

  @GetMapping("/products/{sku}")
  public ResponseEntity<?> get(@PathVariable("sku") String sku) {
    return service.getBySku(sku)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "not_found", "sku", sku)));
  }

  @PostMapping(value = "/products", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Product upsert(@Valid @RequestBody ProductDto dto) {
    return service.upsert(dto);
  }

  @GetMapping("/products/search")
  public Object search(@RequestParam(name = "q", required = false) String q) {
    return Map.of("q", q, "rows", legacySearch.searchByNameUnsafe(q));
  }
}

