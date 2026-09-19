package com.kimngeam.backend.rag.ingestion;

import com.pgvector.PGvector;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Escribe el vector en {@code corpus_chunk.embedding} por JDBC directo, no
 * por JPA: la entidad {@link com.kimngeam.backend.shared.entity.CorpusChunk}
 * no mapea esa columna a propósito (ver su Javadoc). {@code PGvector} viene
 * de {@code com.pgvector:pgvector}, ya en el classpath vía el starter de
 * pgvector de Spring AI.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.corpus.ingestion", name = "enabled", havingValue = "true")
public class CorpusChunkEmbeddingWriter {

	private static final String UPDATE_SQL = "UPDATE corpus_chunk SET embedding = ?, modelo_embedding = ? WHERE id = ?";

	private final JdbcTemplate jdbcTemplate;

	public CorpusChunkEmbeddingWriter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void escribir(UUID chunkId, float[] embedding, String modeloEmbedding) {
		jdbcTemplate.update(UPDATE_SQL, statement -> vincularParametros(statement, chunkId, embedding, modeloEmbedding));
	}

	private void vincularParametros(PreparedStatement statement, UUID chunkId, float[] embedding,
			String modeloEmbedding) throws SQLException {
		statement.setObject(1, new PGvector(embedding));
		statement.setString(2, modeloEmbedding);
		statement.setObject(3, chunkId);
	}
}
