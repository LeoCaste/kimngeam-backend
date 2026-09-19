package com.kimngeam.backend.shared.entity;

/**
 * Dirección de una traducción. Contexto cultural solo se genera en
 * {@code ES_MAP} (ver CLAUDE.md) — en {@code MAP_ES} el array de contextos va
 * vacío, comportamiento esperado, no un bug.
 */
public enum Direccion {
	ES_MAP,
	MAP_ES;

	/**
	 * Valor con guión usado en la columna {@code direccion} y en el contrato
	 * de API (ej. {@code "es-map"}) — no calza con {@code name().toLowerCase()}
	 * por el guión, de ahí el mapeo explícito en vez de derivarlo.
	 */
	public String toValue() {
		return switch (this) {
			case ES_MAP -> "es-map";
			case MAP_ES -> "map-es";
		};
	}

	public static Direccion fromValue(String value) {
		return switch (value) {
			case "es-map" -> ES_MAP;
			case "map-es" -> MAP_ES;
			default -> throw new IllegalArgumentException("Dirección inválida: " + value);
		};
	}
}
