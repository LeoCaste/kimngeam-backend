package com.kimngeam.backend.admin.validaciones.dto;

import java.util.List;

public record ValidacionesAdminResponse(List<ValidacionAdminResponse> validaciones, long total) {
}
