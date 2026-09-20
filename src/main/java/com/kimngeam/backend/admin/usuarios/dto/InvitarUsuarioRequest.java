package com.kimngeam.backend.admin.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InvitarUsuarioRequest(@NotBlank(message = "El nombre es obligatorio") String nombre,
		@NotBlank(message = "El email es obligatorio") @Email(message = "El email no es válido") String email) {
}
