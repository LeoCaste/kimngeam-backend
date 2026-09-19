package com.kimngeam.backend.traductor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Umbrales de segmentación del texto de entrada (ver {@link TextSegmenter}).
 * {@code maximoEntrada} rechaza el request (400) por sobre ese largo —
 * protección contra abuso, cada traducción cuesta dinero. Bajo
 * {@code umbralSegmentacion} el texto se traduce completo en un solo
 * segmento; sobre él se parte por oraciones.
 */
@ConfigurationProperties(prefix = "kimngeam.traductor.segmentacion")
public record SegmentacionProperties(@DefaultValue("5000") int maximoEntrada,
		@DefaultValue("200") int umbralSegmentacion) {
}
