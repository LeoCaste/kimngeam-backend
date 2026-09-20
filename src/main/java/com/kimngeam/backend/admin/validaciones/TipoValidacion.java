package com.kimngeam.backend.admin.validaciones;

/**
 * El {@code tipo} del contrato ({@code "expresion"} / {@code "general"})
 * nunca se persiste: se deriva de si {@code Validacion.expresion} es
 * {@code null} (ver docs/API_CONTRACTS.md). Compartido entre
 * {@code admin/validaciones}, {@code admin/usuarios} y {@code admin/dashboard}
 * — todos muestran validaciones en algún shape.
 */
public enum TipoValidacion {
	EXPRESION,
	GENERAL;

	public String toValue() {
		return switch (this) {
			case EXPRESION -> "expresion";
			case GENERAL -> "general";
		};
	}

	public static TipoValidacion desde(String expresion) {
		return expresion == null ? GENERAL : EXPRESION;
	}

	public static TipoValidacion fromValue(String value) {
		return switch (value) {
			case "expresion" -> EXPRESION;
			case "general" -> GENERAL;
			default -> throw new IllegalArgumentException("tipo inválido: " + value);
		};
	}
}
