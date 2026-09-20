package com.kimngeam.backend.admin.usuarios;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Umbral para derivar {@code estado = "sin-actividad"} en
 * {@code GET /admin/usuarios} (ver CLAUDE.md): un usuario activo cuyo
 * {@code ultimo_acceso} es nulo o anterior a este umbral. Nunca se persiste
 * como columna, se deriva en cada consulta.
 */
@ConfigurationProperties(prefix = "kimngeam.admin.usuarios")
public record UmbralActividadProperties(@DefaultValue("30d") Duration umbralSinActividad,
		@DefaultValue("10") int validacionesRecientesLimite) {
}
