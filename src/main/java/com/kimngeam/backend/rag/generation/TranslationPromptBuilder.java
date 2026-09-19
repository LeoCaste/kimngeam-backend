package com.kimngeam.backend.rag.generation;

import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.Direccion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Arma el prompt de un segmento a partir de la plantilla versionada
 * {@code prompts/segmento-traduccion.txt} — nunca hardcodeada en el código,
 * porque va a iterar mucho (ver CLAUDE.md, Fase 3). No usa el
 * {@code PromptTemplate} de Spring AI: su motor ST4 usa {@code {}} como
 * delimitador por defecto, lo que chocaría con las llaves literales del
 * shape JSON de ejemplo que la plantilla le muestra al modelo. El reemplazo
 * de placeholders acá es una simple sustitución de texto.
 */
@Component
public class TranslationPromptBuilder {

	private static final String RECURSO_TEMPLATE = "prompts/segmento-traduccion.txt";

	private final String template;

	public TranslationPromptBuilder() {
		this.template = leerTemplate();
	}

	public String construir(Direccion direccion, String textoSegmento, String textoAnterior,
			String traduccionAnterior, List<RetrievedCorpusChunk> chunksReferencia) {
		return template.replace("{{idioma_origen}}", idiomaOrigen(direccion))
				.replace("{{idioma_destino}}", idiomaDestino(direccion))
				.replace("{{contexto_previo}}", construirContextoPrevio(textoAnterior, traduccionAnterior))
				.replace("{{material_referencia}}", construirMaterialReferencia(chunksReferencia))
				.replace("{{texto_segmento}}", textoSegmento)
				.replace("{{instrucciones_contexto_cultural}}", construirInstruccionesContextoCultural(direccion));
	}

	private String idiomaOrigen(Direccion direccion) {
		return direccion == Direccion.ES_MAP ? "español" : "mapudungun";
	}

	private String idiomaDestino(Direccion direccion) {
		return direccion == Direccion.ES_MAP ? "mapudungun" : "español";
	}

	private String construirContextoPrevio(String textoAnterior, String traduccionAnterior) {
		if (textoAnterior == null) {
			return "";
		}
		return """
				Contexto de la oración anterior (NO la traduzcas, es solo para que entiendas de qué se venía hablando):
				Original: "%s"
				Traducción: "%s"

				""".formatted(textoAnterior, traduccionAnterior);
	}

	private String construirMaterialReferencia(List<RetrievedCorpusChunk> chunks) {
		if (chunks.isEmpty()) {
			return "(sin referencias del corpus disponibles para este texto)";
		}
		return chunks.stream()
				.map(chunk -> "- [%s] %s".formatted(chunk.sourceType().toValue(), chunk.contenido()))
				.collect(Collectors.joining("\n"));
	}

	private String construirInstruccionesContextoCultural(Direccion direccion) {
		if (direccion != Direccion.ES_MAP) {
			return "Esta traducción es hacia el español; no agregues contexto cultural, deja \"contexto_cultural\" como array vacío.\n\n";
		}
		return """
				Además, identifica expresiones culturalmente significativas del texto original o de tu traducción, y agrégalas en "contexto_cultural" explicando su significado o relevancia (usa null en "comunidad" o "variante" si no los conoces con certeza).

				""";
	}

	private String leerTemplate() {
		try (InputStream inputStream = new ClassPathResource(RECURSO_TEMPLATE).getInputStream()) {
			return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new IllegalStateException("No se pudo leer la plantilla de prompt: " + RECURSO_TEMPLATE, e);
		}
	}
}
