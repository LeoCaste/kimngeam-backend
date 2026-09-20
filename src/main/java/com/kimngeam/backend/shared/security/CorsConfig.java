package com.kimngeam.backend.shared.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * El frontend (Vue 3 + Vite, otro repo) corre en un origen distinto al de
 * esta API. Orígenes tomados de {@link CorsProperties}, siempre una lista
 * explícita por configuración — nunca {@code "*"} (ver CLAUDE.md, Fase 6):
 * la API maneja tokens JWT, y un wildcard combinado con credenciales
 * expondría la API a cualquier origen. Consumida por
 * {@code SecurityConfig#securityFilterChain} vía {@code http.cors(...)}.
 */
@Configuration
public class CorsConfig {

	private final CorsProperties corsProperties;

	public CorsConfig(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(corsProperties.allowedOrigins());
		configuration.setAllowedMethods(
				List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PATCH.name(),
						HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
		configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
