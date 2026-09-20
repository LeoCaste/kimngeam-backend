package com.kimngeam.backend.shared.util;

/**
 * El campo {@code inicial} de un usuario (primera letra del nombre) lo
 * calcula el backend al construir cada response, nunca se persiste (ver
 * CLAUDE.md). Compartido entre {@code admin.usuarios}, {@code admin.validaciones}
 * y {@code admin.dashboard} — los tres muestran usuarios en algún shape.
 */
public final class Iniciales {

	private Iniciales() {
	}

	public static String de(String nombre) {
		return nombre.substring(0, 1).toUpperCase();
	}
}
