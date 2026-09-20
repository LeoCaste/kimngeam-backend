package com.kimngeam.backend.traductor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class TraductorRateLimiterTest {

	@Test
	void permiteHastaElMaximoConfiguradoPorCliente() {
		TraductorRateLimiter rateLimiter = new TraductorRateLimiter(new RateLimitProperties(3, Duration.ofMinutes(1)));

		assertThat(rateLimiter.permitir("ip-1")).isTrue();
		assertThat(rateLimiter.permitir("ip-1")).isTrue();
		assertThat(rateLimiter.permitir("ip-1")).isTrue();
		assertThat(rateLimiter.permitir("ip-1")).isFalse();
	}

	@Test
	void clientesDistintosTienenVentanasIndependientes() {
		TraductorRateLimiter rateLimiter = new TraductorRateLimiter(new RateLimitProperties(1, Duration.ofMinutes(1)));

		assertThat(rateLimiter.permitir("ip-1")).isTrue();
		assertThat(rateLimiter.permitir("ip-1")).isFalse();
		assertThat(rateLimiter.permitir("ip-2")).isTrue();
	}

	@Test
	void vencidaLaVentanaElContadorSeReinicia() throws InterruptedException {
		TraductorRateLimiter rateLimiter = new TraductorRateLimiter(
				new RateLimitProperties(1, Duration.ofMillis(50)));

		assertThat(rateLimiter.permitir("ip-1")).isTrue();
		assertThat(rateLimiter.permitir("ip-1")).isFalse();

		Thread.sleep(80);

		assertThat(rateLimiter.permitir("ip-1")).isTrue();
	}
}
