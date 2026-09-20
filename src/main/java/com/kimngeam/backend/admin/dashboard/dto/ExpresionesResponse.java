package com.kimngeam.backend.admin.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cuatro conteos independientes sobre traducciones — no un desglose que sume
 * {@code total} (ver CLAUDE.md, decisión acordada con el usuario en Fase 5):
 * {@code total}, {@code con_contexto} y {@code sin_contexto} se calculan
 * sobre traducciones {@code es-map} (el contexto cultural solo existe en esa
 * dirección); {@code sin_validar} se calcula sobre traducciones de ambas
 * direcciones, porque una traducción {@code map-es} también puede validarse.
 */
public record ExpresionesResponse(long total, @JsonProperty("con_contexto") long conContexto,
		@JsonProperty("sin_validar") long sinValidar, @JsonProperty("sin_contexto") long sinContexto) {
}
