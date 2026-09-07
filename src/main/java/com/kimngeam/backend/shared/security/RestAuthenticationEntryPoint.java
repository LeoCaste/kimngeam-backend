package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Se dispara cuando una request sin autenticar (o con token inválido/expirado)
 * llega a un endpoint protegido. Corre a nivel de filtro, fuera del
 * DispatcherServlet, así que escribe el JSON del contrato directamente en vez
 * de delegar en {@link com.kimngeam.backend.shared.error.GlobalExceptionHandler}.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), ErrorResponse.of("UNAUTHORIZED", "Token inválido o expirado"));
	}
}
