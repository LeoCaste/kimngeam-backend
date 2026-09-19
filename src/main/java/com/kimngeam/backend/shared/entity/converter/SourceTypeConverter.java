package com.kimngeam.backend.shared.entity.converter;

import com.kimngeam.backend.shared.entity.SourceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SourceTypeConverter implements AttributeConverter<SourceType, String> {

	@Override
	public String convertToDatabaseColumn(SourceType sourceType) {
		return sourceType == null ? null : sourceType.toValue();
	}

	@Override
	public SourceType convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : SourceType.fromValue(dbValue);
	}
}
