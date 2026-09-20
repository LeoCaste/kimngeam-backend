package com.kimngeam.backend.validaciones;

import com.kimngeam.backend.rag.embedding.EmbeddingModelIdentifier;
import com.kimngeam.backend.rag.ingestion.CorpusChunkEmbeddingWriter;
import com.kimngeam.backend.shared.entity.CorpusChunk;
import com.kimngeam.backend.shared.entity.CorpusChunkRepository;
import com.kimngeam.backend.shared.entity.SourceType;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.Validacion;
import com.kimngeam.backend.shared.error.NotFoundException;
import java.time.OffsetDateTime;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * El corazón de Fase 4 (ver CLAUDE.md): la validación de un académico entra
 * directo al corpus como material de primera clase — {@code source_type =
 * expert_feedback}, {@code validado = true}, sin segunda aprobación. Separado
 * de {@link ValidacionService} a propósito (responsabilidad única): ese
 * servicio persiste la validación en sí, este indexa su efecto sobre el
 * corpus.
 */
@Service
public class ValidacionCorpusIndexer {

	private final CorpusChunkRepository corpusChunkRepository;
	private final CorpusChunkEmbeddingWriter embeddingWriter;
	private final EmbeddingModel embeddingModel;
	private final EmbeddingModelIdentifier embeddingModelIdentifier;

	public ValidacionCorpusIndexer(CorpusChunkRepository corpusChunkRepository,
			CorpusChunkEmbeddingWriter embeddingWriter, EmbeddingModel embeddingModel,
			EmbeddingModelIdentifier embeddingModelIdentifier) {
		this.corpusChunkRepository = corpusChunkRepository;
		this.embeddingWriter = embeddingWriter;
		this.embeddingModel = embeddingModel;
		this.embeddingModelIdentifier = embeddingModelIdentifier;
	}

	public void indexar(Validacion validacion, Usuario academico) {
		String contenido = construirContenido(validacion);
		CorpusChunk chunk = new CorpusChunk(SourceType.EXPERT_FEEDBACK, validacion.getId(), contenido,
				validacion.getVariante(), null, true);
		chunk.setValidadoPor(academico);
		chunk.setValidadoEn(OffsetDateTime.now());
		corpusChunkRepository.save(chunk);
		// El INSERT tiene que llegar a la base antes del UPDATE por JDBC
		// directo de más abajo: sin este flush, Hibernate puede diferirlo (con
		// una transacción envolvente todavía abierta) hasta un autoflush
		// posterior, que reinsertaría la fila y pisaría embedding/modelo_embedding
		// con null.
		corpusChunkRepository.flush();

		float[] vector = embeddingModel.embed(contenido);
		embeddingWriter.escribir(chunk.getId(), vector, embeddingModelIdentifier.identificador());
	}

	/**
	 * Reversibilidad (ver CLAUDE.md, Fase 4): un chunk de {@code expert_feedback}
	 * se desactiva, nunca se borra — si un académico valida algo por error,
	 * tiene que poder revertirse sin perder la historia. Sin endpoint
	 * todavía que lo exponga; el mecanismo queda listo para cuando lo haya.
	 */
	public void revertir(String validacionId) {
		CorpusChunk chunk = corpusChunkRepository
				.findBySourceTypeAndSourceRef(SourceType.EXPERT_FEEDBACK, validacionId)
				.orElseThrow(
						() -> new NotFoundException("No hay un chunk de corpus para la validación " + validacionId));
		chunk.setActivo(false);
		corpusChunkRepository.save(chunk);
	}

	private String construirContenido(Validacion validacion) {
		if (validacion.getExpresion() == null) {
			return validacion.getTexto();
		}
		return validacion.getExpresion() + ": " + validacion.getTexto();
	}
}
