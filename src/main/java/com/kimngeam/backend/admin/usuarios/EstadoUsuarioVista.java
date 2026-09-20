package com.kimngeam.backend.admin.usuarios;

/**
 * El {@code estado} de {@code GET /admin/usuarios}, con tres valores —
 * distinto de {@link com.kimngeam.backend.shared.entity.EstadoUsuario}, que
 * solo persiste dos ({@code activo}/{@code inactivo}). {@code SIN_ACTIVIDAD}
 * se deriva, nunca se guarda (ver CLAUDE.md).
 */
public enum EstadoUsuarioVista {
	ACTIVO,
	INACTIVO,
	SIN_ACTIVIDAD;

	public String toValue() {
		return switch (this) {
			case ACTIVO -> "activo";
			case INACTIVO -> "inactivo";
			case SIN_ACTIVIDAD -> "sin-actividad";
		};
	}
}
