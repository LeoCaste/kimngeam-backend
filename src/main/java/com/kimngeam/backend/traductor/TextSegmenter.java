package com.kimngeam.backend.traductor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Segmentación condicional del texto de entrada: bajo el umbral se traduce
 * completo (un solo segmento, con {@code orden = 1}, para que el modelo de
 * datos sea uniforme); sobre el umbral se parte por oraciones. El corte de
 * oración es una heurística simple (puntuación + espacio) — no resuelve
 * abreviaturas tipo "Dr.", aceptable para los textos cortos que recibe el
 * traductor.
 */
@Component
public class TextSegmenter {

	private static final Pattern LIMITE_ORACION = Pattern.compile("(?<=[.!?])\\s+");

	public List<String> segmentar(String texto, int umbralSegmentacion) {
		String textoLimpio = texto.strip();
		if (textoLimpio.length() <= umbralSegmentacion) {
			return List.of(textoLimpio);
		}
		List<String> oraciones = Arrays.stream(LIMITE_ORACION.split(textoLimpio))
				.map(String::strip)
				.filter(oracion -> !oracion.isEmpty())
				.toList();
		return oraciones.isEmpty() ? List.of(textoLimpio) : oraciones;
	}
}
