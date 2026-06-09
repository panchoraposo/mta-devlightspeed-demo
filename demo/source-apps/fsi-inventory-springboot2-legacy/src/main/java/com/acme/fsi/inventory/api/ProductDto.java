package com.acme.fsi.inventory.api;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class ProductDto {
  @NotBlank
  private String sku;

  @NotBlank
  private String name;

  @Min(0)
  private int quantity;

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}

