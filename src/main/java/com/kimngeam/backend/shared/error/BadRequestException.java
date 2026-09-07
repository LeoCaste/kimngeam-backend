package com.kimngeam.backend.shared.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
	}

	public BadRequestException(String code, String message) {
		super(HttpStatus.BAD_REQUEST, code, message);
	}
}
