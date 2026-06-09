package com.acme.fsi.risk;

import java.security.MessageDigest;

public final class LegacyWeakCrypto {
  private LegacyWeakCrypto() {}

  public static byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((s == null ? "" : s).getBytes("UTF-8"));
      return md.digest();
    } catch (Exception e) {
      return new byte[0];
    }
  }
}

