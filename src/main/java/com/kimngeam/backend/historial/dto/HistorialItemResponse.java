package com.kimngeam.backend.historial.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record HistorialItemResponse(String id, @JsonProperty("texto_origen") String textoOrigen,
		@JsonProperty("texto_traducido") String textoTraducido, String direccion, OffsetDateTime fecha) {
}
