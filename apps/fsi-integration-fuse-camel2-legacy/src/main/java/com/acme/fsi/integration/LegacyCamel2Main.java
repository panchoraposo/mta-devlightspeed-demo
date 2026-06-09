package com.acme.fsi.integration;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LegacyCamel2Main {
  public static void main(String[] args) {
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("camel-context.xml");
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          ctx.close();
        } catch (Exception ignored) {}
      }
    }));
    System.out.println("Legacy Camel 2 integration started. Press Ctrl+C to stop.");
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}

