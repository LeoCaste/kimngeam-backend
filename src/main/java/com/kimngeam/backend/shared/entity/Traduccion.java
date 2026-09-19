package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Una traducción es la unidad central de trazabilidad del proyecto (ver
 * CLAUDE.md): de ella cuelgan sus {@link TraduccionSegmento} y, a través de
 * estos, los {@link TraduccionFuente} que la respaldaron. {@code id} se
 * genera en Java (no con el default de la columna en {@code V1__init_schema.sql})
 * para no depender de leer de vuelta un valor generado por Postgres tras el
 * insert; sigue el mismo formato ({@code "trad_" + uuid sin guiones}) que ese
 * default, así que el resultado es indistinguible.
 */
@Entity
@Table(name = "traduccion")
public class Traduccion {

	private static final String PREFIJO_ID = "trad_";

	@Id
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "texto_origen", nullable = false)
	private String textoOrigen;

	@Column(name = "texto_traducido", nullable = false)
	private String textoTraducido;

	@Column(nullable = false)
	private Direccion direccion;

	@Column
	private BigDecimal confianza;

	@Column(name = "modelo_llm")
	private String modeloLlm;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime fecha;

	protected Traduccion() {
	}

	public Traduccion(Usuario usuario, String textoOrigen, String textoTraducido, Direccion direccion,
			BigDecimal confianza, String modeloLlm) {
		this.id = PREFIJO_ID + UUID.randomUUID().toString().replace("-", "");
		this.usuario = usuario;
		this.textoOrigen = textoOrigen;
		this.textoTraducido = textoTraducido;
		this.direccion = direccion;
		this.confianza = confianza;
		this.modeloLlm = modeloLlm;
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

	public String getTextoOrigen() {
		return textoOrigen;
	}

	public String getTextoTraducido() {
		return textoTraducido;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public BigDecimal getConfianza() {
		return confianza;
	}

	public String getModeloLlm() {
		return modeloLlm;
	}

	public OffsetDateTime getFecha() {
		return fecha;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Traduccion that)) {
			return false;
		}
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
