package com.kimngeam.backend.shared.error;

/**
 * Formato de error estándar de toda la API (ver CLAUDE.md / API_CONTRACTS.md).
 */
public record ErrorResponse(boolean error, String code, String message) {

	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(true, code, message);
	}
}
