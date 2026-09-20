package com.kimngeam.backend.traductor;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros de {@link ConfianzaCalculator}. El score crudo de similitud
 * coseno de bge-m3 queda comprimido justo por encima de
 * {@code kimngeam.traductor.umbral-similitud}: en una medición sobre las 10
 * consultas de referencia de {@code CorpusRetrievalServiceIntegrationTest}
 * (Fase 5, item 8 — ver docs/TODO.md), el score máximo observado fue 0.7154 y
 * dos traducciones con respaldo muy distinto (5 chunks centrales del corpus
 * vs. 2 chunks tangenciales) dieron confianzas casi indistinguibles (0.56 vs
 * 0.51) porque el promedio crudo apenas se aleja del umbral. Por eso
 * {@code techoSimilitud} reescala ese rango real en vez de [0, 1] —
 * recalibrarlo si el corpus cambia sustancialmente, mismo criterio que
 * {@code umbral-similitud}.
 */
@ConfigurationProperties(prefix = "kimngeam.traductor.confianza")
public record ConfianzaProperties(@DefaultValue("1.5") BigDecimal pesoChunkValidado,
		@DefaultValue("0.72") double techoSimilitud, @DefaultValue("0.5") double pesoMaximo,
		@DefaultValue("3") int chunksParaCoberturaCompleta) {
}
