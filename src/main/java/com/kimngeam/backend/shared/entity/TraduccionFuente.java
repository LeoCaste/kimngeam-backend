package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Un chunk de {@code corpus_chunk} que respaldó un {@link TraduccionSegmento},
 * con el score de similitud que tuvo en el retrieval. Es la trazabilidad que
 * permite reconstruir por qué el modelo tradujo lo que tradujo (ver CLAUDE.md).
 */
@Entity
@Table(name = "traduccion_fuente")
public class TraduccionFuente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "segmento_id", nullable = false)
	private TraduccionSegmento segmento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "corpus_chunk_id", nullable = false)
	private CorpusChunk corpusChunk;

	@Column(nullable = false)
	private BigDecimal score;

	protected TraduccionFuente() {
	}

	public TraduccionFuente(TraduccionSegmento segmento, CorpusChunk corpusChunk, BigDecimal score) {
		this.segmento = segmento;
		this.corpusChunk = corpusChunk;
		this.score = score;
	}

	public Long getId() {
		return id;
	}

	public TraduccionSegmento getSegmento() {
		return segmento;
	}

	public CorpusChunk getCorpusChunk() {
		return corpusChunk;
	}

	public BigDecimal getScore() {
		return score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TraduccionFuente that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
