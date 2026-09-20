package com.kimngeam.backend.admin.usuarios;

import static org.assertj.core.api.Assertions.assertThat;

import com.kimngeam.backend.shared.entity.EstadoUsuario;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class EstadoUsuarioCalculatorTest {

	private final EstadoUsuarioCalculator calculator = new EstadoUsuarioCalculator(
			new UmbralActividadProperties(Duration.ofDays(30), 10));

	@Test
	void usuarioInactivoEsInactivoSinImportarUltimoAcceso() {
		EstadoUsuarioVista estado = calculator.calcular(EstadoUsuario.INACTIVO, OffsetDateTime.now());

		assertThat(estado).isEqualTo(EstadoUsuarioVista.INACTIVO);
	}

	@Test
	void usuarioActivoConAccesoRecienteEsActivo() {
		EstadoUsuarioVista estado = calculator.calcular(EstadoUsuario.ACTIVO, OffsetDateTime.now().minusDays(1));

		assertThat(estado).isEqualTo(EstadoUsuarioVista.ACTIVO);
	}

	@Test
	void usuarioActivoSinUltimoAccesoEsSinActividad() {
		EstadoUsuarioVista estado = calculator.calcular(EstadoUsuario.ACTIVO, null);

		assertThat(estado).isEqualTo(EstadoUsuarioVista.SIN_ACTIVIDAD);
	}

	@Test
	void usuarioActivoConAccesoAnteriorAlUmbralEsSinActividad() {
		EstadoUsuarioVista estado = calculator.calcular(EstadoUsuario.ACTIVO, OffsetDateTime.now().minusDays(31));

		assertThat(estado).isEqualTo(EstadoUsuarioVista.SIN_ACTIVIDAD);
	}

	@Test
	void usuarioActivoJustoEnElUmbralEsSinActividad() {
		EstadoUsuarioVista estado = calculator.calcular(EstadoUsuario.ACTIVO, OffsetDateTime.now().minusDays(30));

		assertThat(estado).isEqualTo(EstadoUsuarioVista.SIN_ACTIVIDAD);
	}
}
