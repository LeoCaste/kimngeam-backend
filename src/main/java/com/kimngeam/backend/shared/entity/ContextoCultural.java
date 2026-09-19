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

/**
 * Un aporte cultural asociado a una {@link Traduccion}, generado solo cuando
 * {@code direccion = ES_MAP} (ver CLAUDE.md). {@code variante} es un String
 * simple, no una relación JPA, porque la tabla {@code variante} usa el
 * nombre como llave natural — mismo criterio que {@link CorpusChunk#getVariante()}.
 */
@Entity
@Table(name = "contexto_cultural")
public class ContextoCultural {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "traduccion_id", nullable = false)
	private Traduccion traduccion;

	@Column(nullable = false)
	private String expresion;

	@Column(nullable = false)
	private String aporte;

	@Column
	private String comunidad;

	@Column
	private String variante;

	protected ContextoCultural() {
	}

	public ContextoCultural(Traduccion traduccion, String expresion, String aporte, String comunidad,
			String variante) {
		this.traduccion = traduccion;
		this.expresion = expresion;
		this.aporte = aporte;
		this.comunidad = comunidad;
		this.variante = variante;
	}

	public Long getId() {
		return id;
	}

	public Traduccion getTraduccion() {
		return traduccion;
	}

	public String getExpresion() {
		return expresion;
	}

	public String getAporte() {
		return aporte;
	}

	public String getComunidad() {
		return comunidad;
	}

	public String getVariante() {
		return variante;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ContextoCultural that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
