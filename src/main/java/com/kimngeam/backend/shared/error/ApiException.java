package com.kimngeam.backend.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Base para excepciones de dominio con status y code HTTP propios, traducidas
 * 1:1 al formato de error estándar por {@link GlobalExceptionHandler}.
 */
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	protected ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
