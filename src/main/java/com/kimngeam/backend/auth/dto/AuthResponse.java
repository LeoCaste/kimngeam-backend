package com.kimngeam.backend.auth.dto;

public record AuthResponse(String token, UsuarioResponse usuario) {
}
