package com.kimngeam.backend.shared.entity.converter;

import com.kimngeam.backend.shared.entity.EstadoUsuario;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoUsuarioConverter implements AttributeConverter<EstadoUsuario, String> {

	@Override
	public String convertToDatabaseColumn(EstadoUsuario estado) {
		return estado == null ? null : estado.name().toLowerCase();
	}

	@Override
	public EstadoUsuario convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : EstadoUsuario.valueOf(dbValue.toUpperCase());
	}
}
