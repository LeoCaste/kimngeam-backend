package com.kimngeam.backend.traductor;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cuánto más pesa un chunk validado ({@code corpus_chunk.validado = true}) al
 * calcular la confianza de un segmento, frente al peso base 1 de un chunk sin
 * revisar (ver {@link ConfianzaCalculator}).
 */
@ConfigurationProperties(prefix = "kimngeam.traductor.confianza")
public record ConfianzaProperties(@DefaultValue("1.5") BigDecimal pesoChunkValidado) {
}
