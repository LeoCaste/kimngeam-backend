package com.kimngeam.backend.shared.error;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

	public UnauthorizedException(String message) {
		super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
	}
}
