package com.kimngeam.backend.traductor;

import com.kimngeam.backend.shared.entity.Direccion;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resultado de {@link TraductorService#traducir}, ya persistido. Deliberadamente
 * no es la entidad {@code Traduccion} (ver CLAUDE.md, "DTOs siempre en los
 * bordes"): quien construya el endpoint en el bloque 3 mapea esto 1:1 al
 * shape del contrato de {@code POST /traductor/traducir}.
 */
public record TraduccionResultado(String id, String textoOrigen, String textoTraducido, Direccion direccion,
		BigDecimal confianza, List<ContextoCulturalResultado> contextos, OffsetDateTime fecha) {
}
