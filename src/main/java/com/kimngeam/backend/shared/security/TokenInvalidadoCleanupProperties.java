package com.kimngeam.backend.shared.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Frecuencia del job que purga {@code token_invalidado} (ver
 * {@link TokenInvalidadoCleanupJob}). Default de una hora: la tabla solo
 * crece con logouts, sin urgencia de purgarla más seguido.
 */
@ConfigurationProperties(prefix = "kimngeam.auth.token-invalidado-cleanup")
public record TokenInvalidadoCleanupProperties(@DefaultValue("1h") Duration intervalo) {
}
