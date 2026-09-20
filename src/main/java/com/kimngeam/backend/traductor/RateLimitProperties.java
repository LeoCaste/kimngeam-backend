package com.kimngeam.backend.traductor;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Límite de solicitudes a {@code POST /traductor/traducir} por cliente
 * (identificado por IP) dentro de una ventana de tiempo — cada llamada
 * cuesta dinero y el endpoint acepta requests anónimas, así que es
 * abusable (ver CLAUDE.md, Fase 3, bloque 3). Defaults conservadores como
 * punto de partida: 10 solicitudes por minuto.
 */
@ConfigurationProperties(prefix = "kimngeam.traductor.rate-limit")
public record RateLimitProperties(@DefaultValue("10") int maxRequests, @DefaultValue("1m") Duration ventana) {
}
