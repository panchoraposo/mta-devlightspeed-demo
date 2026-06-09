package com.acme.fsi.inventory.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {
  @Value("${inventory.admin.user:admin}")
  String adminUser;

  @Value("${inventory.admin.password:admin123}")
  String adminPassword;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestParam("user") String user, @RequestParam("password") String password) {
    if (adminUser.equals(user) && adminPassword.equals(password)) {
      return ResponseEntity.ok(Map.of("status", "OK", "token", user + "-token"));
    }
    return ResponseEntity.status(401).body(Map.of("status", "DENIED"));
  }
}

