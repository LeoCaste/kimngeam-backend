package com.kimngeam.backend.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "token_invalidado")
public class TokenInvalidado {

	@Id
	private String jti;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(name = "expira_en", nullable = false)
	private OffsetDateTime expiraEn;

	protected TokenInvalidado() {
	}

	public TokenInvalidado(String jti, Long usuarioId, OffsetDateTime expiraEn) {
		this.jti = jti;
		this.usuarioId = usuarioId;
		this.expiraEn = expiraEn;
	}

	public String getJti() {
		return jti;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public OffsetDateTime getExpiraEn() {
		return expiraEn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TokenInvalidado that)) {
			return false;
		}
		return jti != null && jti.equals(that.jti);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
