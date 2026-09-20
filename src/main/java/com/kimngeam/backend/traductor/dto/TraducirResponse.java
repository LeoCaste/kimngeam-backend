package com.kimngeam.backend.traductor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kimngeam.backend.traductor.ContextoCulturalResultado;
import com.kimngeam.backend.traductor.TraduccionResultado;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Shape exacto de {@code POST /traductor/traducir} (ver
 * {@code docs/API_CONTRACTS.md}), con {@code advertencias} como extensión
 * deliberada del contrato: cuando ningún segmento tuvo respaldo del corpus,
 * el sistema lo declara explícito en vez de dejar que una confianza baja
 * pase desapercibida (ver CLAUDE.md, Fase 3, bloque 3).
 */
public record TraducirResponse(String id, @JsonProperty("texto_origen") String textoOrigen,
		@JsonProperty("texto_traducido") String textoTraducido, String direccion,
		List<ContextoCulturalResponse> contextos, List<String> advertencias, OffsetDateTime fecha) {

	private static final String ADVERTENCIA_SIN_RESPALDO = "Ningún fragmento de esta traducción tuvo respaldo "
			+ "del corpus; no fue posible validarla contra fuentes conocidas.";

	public static TraducirResponse desde(TraduccionResultado resultado) {
		List<ContextoCulturalResponse> contextos = resultado.contextos().stream().map(TraducirResponse::aResponse).toList();
		List<String> advertencias = resultado.sinRespaldo() ? List.of(ADVERTENCIA_SIN_RESPALDO) : List.of();
		return new TraducirResponse(resultado.id(), resultado.textoOrigen(), resultado.textoTraducido(),
				resultado.direccion().toValue(), contextos, advertencias, resultado.fecha());
	}

	private static ContextoCulturalResponse aResponse(ContextoCulturalResultado contexto) {
		return new ContextoCulturalResponse(contexto.expresion(), contexto.aporte(), contexto.comunidad(),
				contexto.variante());
	}
}
