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
 * Un segmento (frase u oración) de una traducción, con su propia confianza y
 * si tuvo respaldo del corpus (ver CLAUDE.md, Fase 3: la confianza se deriva
 * del retrieval, nunca se le pide al LLM que la invente).
 */
@Entity
@Table(name = "traduccion_segmento")
public class TraduccionSegmento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "traduccion_id", nullable = false)
	private Traduccion traduccion;

	@Column(nullable = false)
	private int orden;

	@Column(name = "texto_origen", nullable = false)
	private String textoOrigen;

	@Column(name = "texto_traducido", nullable = false)
	private String textoTraducido;

	@Column
	private BigDecimal confianza;

	@Column(name = "con_respaldo", nullable = false)
	private boolean conRespaldo;

	protected TraduccionSegmento() {
	}

	public TraduccionSegmento(Traduccion traduccion, int orden, String textoOrigen, String textoTraducido,
			BigDecimal confianza, boolean conRespaldo) {
		this.traduccion = traduccion;
		this.orden = orden;
		this.textoOrigen = textoOrigen;
		this.textoTraducido = textoTraducido;
		this.confianza = confianza;
		this.conRespaldo = conRespaldo;
	}

	public Long getId() {
		return id;
	}

	public Traduccion getTraduccion() {
		return traduccion;
	}

	public int getOrden() {
		return orden;
	}

	public String getTextoOrigen() {
		return textoOrigen;
	}

	public String getTextoTraducido() {
		return textoTraducido;
	}

	public BigDecimal getConfianza() {
		return confianza;
	}

	public boolean isConRespaldo() {
		return conRespaldo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TraduccionSegmento that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
