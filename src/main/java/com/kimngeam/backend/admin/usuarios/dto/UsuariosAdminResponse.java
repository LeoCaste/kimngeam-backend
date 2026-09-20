package com.kimngeam.backend.admin.usuarios.dto;

import java.util.List;

public record UsuariosAdminResponse(List<UsuarioAdminResponse> usuarios, long total) {
}
