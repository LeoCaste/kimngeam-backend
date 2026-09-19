package com.kimngeam.backend.rag.retrieval;

import com.kimngeam.backend.shared.entity.SourceType;
import com.pgvector.PGvector;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Búsqueda semántica sobre {@code corpus_chunk}: embede la consulta con el
 * mismo {@link EmbeddingModel} activo que indexó el corpus (ver
 * {@code rag.embedding.EmbeddingModelConfig} — vectores de proveedores
 * distintos no son comparables, ver CLAUDE.md) y ordena por similitud coseno
 * usando el índice HNSW de pgvector ({@code idx_corpus_embedding}). La
 * columna {@code embedding} no está mapeada en la entidad JPA (ver su
 * Javadoc), así que esta consulta va por JDBC directo, igual que la
 * escritura en ingesta ({@code CorpusChunkEmbeddingWriter}).
 */
@Service
public class CorpusRetrievalService {

	private static final String SQL_SIN_FILTRO = """
			SELECT id, source_type, source_ref, contenido, variante,
			       1 - (embedding <=> ?) AS similitud
			FROM corpus_chunk
			WHERE activo = true AND embedding IS NOT NULL
			ORDER BY embedding <=> ?
			LIMIT ?
			""";

	private static final String SQL_CON_FILTRO = """
			SELECT id, source_type, source_ref, contenido, variante,
			       1 - (embedding <=> ?) AS similitud
			FROM corpus_chunk
			WHERE activo = true AND embedding IS NOT NULL AND source_type = ?
			ORDER BY embedding <=> ?
			LIMIT ?
			""";

	private static final RowMapper<RetrievedCorpusChunk> ROW_MAPPER = CorpusRetrievalService::mapearFila;

	private final JdbcTemplate jdbcTemplate;
	private final EmbeddingModel embeddingModel;
	private final CorpusRetrievalProperties retrievalProperties;

	public CorpusRetrievalService(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel,
			CorpusRetrievalProperties retrievalProperties) {
		this.jdbcTemplate = jdbcTemplate;
		this.embeddingModel = embeddingModel;
		this.retrievalProperties = retrievalProperties;
	}

	public List<RetrievedCorpusChunk> buscar(String consulta) {
		return buscar(consulta, null);
	}

	public List<RetrievedCorpusChunk> buscar(String consulta, SourceType sourceType) {
		PGvector embeddingConsulta = new PGvector(embeddingModel.embed(consulta));
		int topN = retrievalProperties.topN();
		if (sourceType == null) {
			return jdbcTemplate.query(SQL_SIN_FILTRO, ps -> vincularSinFiltro(ps, embeddingConsulta, topN),
					ROW_MAPPER);
		}
		return jdbcTemplate.query(SQL_CON_FILTRO, ps -> vincularConFiltro(ps, embeddingConsulta, sourceType, topN),
				ROW_MAPPER);
	}

	private void vincularSinFiltro(PreparedStatement statement, PGvector embedding, int topN) throws SQLException {
		statement.setObject(1, embedding);
		statement.setObject(2, embedding);
		statement.setInt(3, topN);
	}

	private void vincularConFiltro(PreparedStatement statement, PGvector embedding, SourceType sourceType, int topN)
			throws SQLException {
		statement.setObject(1, embedding);
		statement.setString(2, sourceType.toValue());
		statement.setObject(3, embedding);
		statement.setInt(4, topN);
	}

	private static RetrievedCorpusChunk mapearFila(ResultSet resultSet, int rowNum) throws SQLException {
		return new RetrievedCorpusChunk((UUID) resultSet.getObject("id"),
				SourceType.fromValue(resultSet.getString("source_type")), resultSet.getString("source_ref"),
				resultSet.getString("contenido"), resultSet.getString("variante"),
				resultSet.getDouble("similitud"));
	}
}
