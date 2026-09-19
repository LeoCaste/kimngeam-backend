package com.kimngeam.backend.shared.entity;

public enum SourceType {
	DICTIONARY,
	TRANSLATION_EXAMPLES,
	EXPERT_FEEDBACK;

	/**
	 * Convierte el valor en snake_case minúsculas usado tanto en la columna
	 * {@code source_type} como en el JSONL de ingesta (ej. "translation_examples").
	 */
	public static SourceType fromValue(String value) {
		return SourceType.valueOf(value.toUpperCase());
	}

	/**
	 * Inverso de {@link #fromValue(String)}: el snake_case minúsculas que
	 * espera la columna {@code source_type}.
	 */
	public String toValue() {
		return name().toLowerCase();
	}
}
