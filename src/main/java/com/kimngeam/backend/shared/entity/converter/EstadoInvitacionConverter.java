package com.kimngeam.backend.shared.entity.converter;

import com.kimngeam.backend.shared.entity.EstadoInvitacion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoInvitacionConverter implements AttributeConverter<EstadoInvitacion, String> {

	@Override
	public String convertToDatabaseColumn(EstadoInvitacion estado) {
		return estado == null ? null : estado.name().toLowerCase();
	}

	@Override
	public EstadoInvitacion convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : EstadoInvitacion.valueOf(dbValue.toUpperCase());
	}
}
