package com.kimngeam.backend.shared.security;

import com.kimngeam.backend.shared.entity.Rol;

/**
 * Principal autenticado, reconstruido a partir de los claims del JWT — sin
 * volver a golpear la base de datos en cada request.
 */
public record AuthenticatedUsuario(Long id, String email, String nombre, Rol rol) {
}
