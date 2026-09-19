package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * La columna {@code embedding} (VECTOR) NO se mapea acá a propósito: no hay
 * un tipo Hibernate para pgvector en el classpath (solo el {@code PGobject}
 * crudo de {@code com.pgvector:pgvector}), y mapearla implicaría atarse a una
 * estrategia antes de resolver el modelo de embeddings (ver CLAUDE.md,
 * "Decisión aún abierta"). Se escribe con JDBC directo en
 * {@code rag.ingestion.CorpusIngestionService} una vez generado el vector.
 */
@Entity
@Table(name = "corpus_chunk")
public class CorpusChunk {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "source_type", nullable = false)
	private SourceType sourceType;

	@Column(name = "source_ref")
	private String sourceRef;

	@Column(nullable = false)
	private String contenido;

	@Column
	private String variante;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column
	private Map<String, Object> metadata;

	@Column(nullable = false)
	private boolean validado = false;

	@Column(name = "modelo_embedding")
	private String modeloEmbedding;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "validado_por")
	private Usuario validadoPor;

	@Column(name = "validado_en")
	private OffsetDateTime validadoEn;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private OffsetDateTime creadoEn;

	protected CorpusChunk() {
	}

	public CorpusChunk(SourceType sourceType, String sourceRef, String contenido, String variante,
			Map<String, Object> metadata, boolean validado) {
		this.sourceType = sourceType;
		this.sourceRef = sourceRef;
		this.contenido = contenido;
		this.variante = variante;
		this.metadata = metadata;
		this.validado = validado;
	}

	@PrePersist
	void prePersist() {
		if (creadoEn == null) {
			creadoEn = OffsetDateTime.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public String getSourceRef() {
		return sourceRef;
	}

	public String getContenido() {
		return contenido;
	}

	public String getVariante() {
		return variante;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public boolean isValidado() {
		return validado;
	}

	public void setValidado(boolean validado) {
		this.validado = validado;
	}

	public String getModeloEmbedding() {
		return modeloEmbedding;
	}

	public void setModeloEmbedding(String modeloEmbedding) {
		this.modeloEmbedding = modeloEmbedding;
	}

	public Usuario getValidadoPor() {
		return validadoPor;
	}

	public void setValidadoPor(Usuario validadoPor) {
		this.validadoPor = validadoPor;
	}

	public OffsetDateTime getValidadoEn() {
		return validadoEn;
	}

	public void setValidadoEn(OffsetDateTime validadoEn) {
		this.validadoEn = validadoEn;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public OffsetDateTime getCreadoEn() {
		return creadoEn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CorpusChunk that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
