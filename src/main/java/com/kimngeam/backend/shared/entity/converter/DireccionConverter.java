package com.kimngeam.backend.shared.entity.converter;

import com.kimngeam.backend.shared.entity.Direccion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DireccionConverter implements AttributeConverter<Direccion, String> {

	@Override
	public String convertToDatabaseColumn(Direccion direccion) {
		return direccion == null ? null : direccion.toValue();
	}

	@Override
	public Direccion convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : Direccion.fromValue(dbValue);
	}
}
