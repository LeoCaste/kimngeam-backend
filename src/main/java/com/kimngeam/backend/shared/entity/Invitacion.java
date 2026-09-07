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

@Entity
@Table(name = "invitacion")
public class Invitacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false, unique = true)
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invitado_por", nullable = false)
	private Usuario invitadoPor;

	@Column(nullable = false)
	private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private OffsetDateTime creadoEn;

	@Column(name = "expira_en", nullable = false)
	private OffsetDateTime expiraEn;

	protected Invitacion() {
	}

	public Invitacion(String nombre, String email, Usuario invitadoPor, String token, OffsetDateTime expiraEn) {
		this.nombre = nombre;
		this.email = email;
		this.invitadoPor = invitadoPor;
		this.token = token;
		this.expiraEn = expiraEn;
	}

	@PrePersist
	void prePersist() {
		if (creadoEn == null) {
			creadoEn = OffsetDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Usuario getInvitadoPor() {
		return invitadoPor;
	}

	public EstadoInvitacion getEstado() {
		return estado;
	}

	public void setEstado(EstadoInvitacion estado) {
		this.estado = estado;
	}

	public String getToken() {
		return token;
	}

	public OffsetDateTime getCreadoEn() {
		return creadoEn;
	}

	public OffsetDateTime getExpiraEn() {
		return expiraEn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Invitacion invitacion)) {
			return false;
		}
		return id != null && id.equals(invitacion.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
