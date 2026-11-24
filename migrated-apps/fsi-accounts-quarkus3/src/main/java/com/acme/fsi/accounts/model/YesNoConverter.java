package com.acme.fsi.accounts.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class YesNoConverter implements AttributeConverter<Boolean, Boolean> {
  @Override
  public Boolean convertToDatabaseColumn(Boolean attribute) {
    return attribute;
  }

  @Override
  public Boolean convertToEntityAttribute(Boolean dbData) {
    return dbData;
  }
}
