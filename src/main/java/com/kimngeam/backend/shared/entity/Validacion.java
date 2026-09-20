package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * El aporte de un académico sobre una traducción — el corazón de Fase 4 (ver
 * CLAUDE.md): esto es lo que alimenta el corpus como {@code expert_feedback}
 * (ver {@code validaciones.ValidacionCorpusIndexer}). {@code expresion} nulo
 * distingue una validación general de una sobre una expresión puntual (el
 * contrato deriva {@code tipo} de esto, nunca se persiste aparte). {@code id}
 * se genera en Java, mismo criterio que {@link Traduccion}.
 */
@Entity
@Table(name = "validacion")
public class Validacion {

	private static final String PREFIJO_ID = "val_";

	@Id
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "traduccion_id", nullable = false)
	private Traduccion traduccion;

	@Column
	private String expresion;

	@Column(nullable = false)
	private String texto;

	@Column
	private String variante;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime fecha;

	protected Validacion() {
	}

	public Validacion(Usuario usuario, Traduccion traduccion, String expresion, String texto, String variante) {
		this.id = PREFIJO_ID + UUID.randomUUID().toString().replace("-", "");
		this.usuario = usuario;
		this.traduccion = traduccion;
		this.expresion = expresion;
		this.texto = texto;
		this.variante = variante;
	}

	@PrePersist
	void prePersist() {
		if (fecha == null) {
			fecha = OffsetDateTime.now();
		}
	}

	public String getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Traduccion getTraduccion() {
		return traduccion;
	}

	public String getExpresion() {
		return expresion;
	}

	public String getTexto() {
		return texto;
	}

	public String getVariante() {
		return variante;
	}

	public OffsetDateTime getFecha() {
		return fecha;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Validacion that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
