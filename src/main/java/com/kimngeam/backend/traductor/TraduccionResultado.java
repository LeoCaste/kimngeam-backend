package com.kimngeam.backend.traductor;

import com.kimngeam.backend.shared.entity.Direccion;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resultado de {@link TraductorService#traducir}, ya persistido. Deliberadamente
 * no es la entidad {@code Traduccion} (ver CLAUDE.md, "DTOs siempre en los
 * bordes"): el bloque 3 lo mapea 1:1 al shape del contrato de
 * {@code POST /traductor/traducir}. {@code sinRespaldo} es {@code true} cuando
 * NINGÚN segmento tuvo respaldo del corpus — el endpoint lo comunica
 * explícito en la respuesta (no solo con una confianza baja): es una decisión
 * de producto, el sistema declara lo que no sabe y eso alimenta la cola de
 * trabajo de los académicos.
 */
public record TraduccionResultado(String id, String textoOrigen, String textoTraducido, Direccion direccion,
		BigDecimal confianza, List<ContextoCulturalResultado> contextos, OffsetDateTime fecha, boolean sinRespaldo) {
}
