package com.kimngeam.backend.admin.usuarios;

import com.kimngeam.backend.shared.entity.EstadoUsuario;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class EstadoUsuarioCalculator {

	private final UmbralActividadProperties properties;

	public EstadoUsuarioCalculator(UmbralActividadProperties properties) {
		this.properties = properties;
	}

	public EstadoUsuarioVista calcular(EstadoUsuario estadoPersistido, OffsetDateTime ultimoAcceso) {
		if (estadoPersistido == EstadoUsuario.INACTIVO) {
			return EstadoUsuarioVista.INACTIVO;
		}
		OffsetDateTime limite = OffsetDateTime.now().minus(properties.umbralSinActividad());
		if (ultimoAcceso == null || ultimoAcceso.isBefore(limite)) {
			return EstadoUsuarioVista.SIN_ACTIVIDAD;
		}
		return EstadoUsuarioVista.ACTIVO;
	}
}
