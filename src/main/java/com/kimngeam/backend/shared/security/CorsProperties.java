package com.kimngeam.backend.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Orígenes explícitos permitidos por CORS (ver {@link CorsConfig}) — nunca
 * {@code "*"}, porque la API maneja tokens JWT en el header
 * {@code Authorization} (ver CLAUDE.md, Fase 6). Default: puerto por defecto
 * de Vite para el frontend prototipo (Vue 3, otro repo) en desarrollo local.
 */
@ConfigurationProperties(prefix = "kimngeam.cors")
public record CorsProperties(@DefaultValue("http://localhost:5173") List<String> allowedOrigins) {
}
