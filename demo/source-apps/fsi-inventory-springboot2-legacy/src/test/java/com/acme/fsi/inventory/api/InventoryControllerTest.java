package com.acme.fsi.inventory.api;

import com.acme.fsi.inventory.InventoryApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InventoryApplication.class)
@AutoConfigureMockMvc
public class InventoryControllerTest {
  @Autowired
  MockMvc mvc;

  @Test
  public void listProducts_ok() throws Exception {
    mvc.perform(get("/api/v1/inventory/products"))
        .andExpect(status().isOk());
  }
}

