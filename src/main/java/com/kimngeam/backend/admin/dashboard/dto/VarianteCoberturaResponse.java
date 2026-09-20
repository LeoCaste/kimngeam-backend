package com.kimngeam.backend.admin.dashboard.dto;

/**
 * {@code cantidad} = cantidad de validaciones registradas con esta variante
 * (variante nula agrupa como {@code "Otros"}). {@code porcentaje} = la
 * participación de esa variante sobre el total de validaciones, NO la
 * cobertura real del corpus para esa variante — hoy no es calculable, porque
 * {@code corpus_chunk.variante} quedó nulo para los chunks de transcripciones
 * orales (los archivos fuente no traen marca dialectal, ver Fase 2 y
 * docs/TODO.md).
 */
public record VarianteCoberturaResponse(String nombre, long cantidad, int porcentaje) {
}
