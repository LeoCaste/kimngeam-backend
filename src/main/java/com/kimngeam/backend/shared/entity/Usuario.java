package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * El campo {@code inicial} del contrato de API NO se persiste: el backend lo
 * calcula (primera letra de {@code nombre}) al construir el response.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column
	private String apellido;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private Rol rol;

	@Column(nullable = false)
	private EstadoUsuario estado = EstadoUsuario.ACTIVO;

	@Column(name = "ultimo_acceso")
	private OffsetDateTime ultimoAcceso;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private OffsetDateTime creadoEn;

	protected Usuario() {
	}

	public Usuario(String nombre, String apellido, String email, String passwordHash, Rol rol) {
		this.nombre = nombre;
		this.apellido = apellido;
		this.email = email;
		this.passwordHash = passwordHash;
		this.rol = rol;
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

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public EstadoUsuario getEstado() {
		return estado;
	}

	public void setEstado(EstadoUsuario estado) {
		this.estado = estado;
	}

	public OffsetDateTime getUltimoAcceso() {
		return ultimoAcceso;
	}

	public void setUltimoAcceso(OffsetDateTime ultimoAcceso) {
		this.ultimoAcceso = ultimoAcceso;
	}

	public OffsetDateTime getCreadoEn() {
		return creadoEn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Usuario usuario)) {
			return false;
		}
		return id != null && id.equals(usuario.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
