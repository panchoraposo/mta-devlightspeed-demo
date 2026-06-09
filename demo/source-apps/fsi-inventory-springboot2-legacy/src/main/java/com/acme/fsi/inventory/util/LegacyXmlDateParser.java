package com.acme.fsi.inventory.util;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Calendar;

public final class LegacyXmlDateParser {
  private LegacyXmlDateParser() {}

  public static Instant parseInstantOrNull(String iso) {
    if (iso == null || iso.trim().isEmpty()) {
      return null;
    }
    try {
      Calendar cal = DatatypeConverter.parseDateTime(iso);
      return cal == null ? null : cal.toInstant();
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}

