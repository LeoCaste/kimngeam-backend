package com.kimngeam.backend.admin.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DashboardResponse(@JsonProperty("total_usuarios") long totalUsuarios,
		@JsonProperty("total_traducciones") long totalTraducciones,
		@JsonProperty("total_validaciones") long totalValidaciones, ExpresionesResponse expresiones,
		List<VarianteCoberturaResponse> variantes,
		@JsonProperty("ultimas_validaciones") List<UltimaValidacionResponse> ultimasValidaciones) {
}
