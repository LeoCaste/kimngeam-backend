package com.kimngeam.backend.traductor;

import com.kimngeam.backend.shared.error.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

	private final TraductorRateLimiter rateLimiter;

	public RateLimitInterceptor(TraductorRateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!rateLimiter.permitir(request.getRemoteAddr())) {
			throw new TooManyRequestsException("Demasiadas solicitudes de traducción; intenta de nuevo más tarde");
		}
		return true;
	}
}
