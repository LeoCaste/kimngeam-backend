package com.kimngeam.backend.shared.entity.converter;

import com.kimngeam.backend.shared.entity.Rol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter implements AttributeConverter<Rol, String> {

	@Override
	public String convertToDatabaseColumn(Rol rol) {
		return rol == null ? null : rol.name().toLowerCase();
	}

	@Override
	public Rol convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : Rol.valueOf(dbValue.toUpperCase());
	}
}
