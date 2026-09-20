package com.kimngeam.backend.traductor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Ventana fija en memoria por cliente: simple y suficiente para una sola
 * instancia (no distribuido). Si el servicio llega a correr en más de una
 * instancia, esto necesita reemplazarse por un límite compartido (ej. Redis).
 */
@Component
public class TraductorRateLimiter {

	private final RateLimitProperties properties;
	private final ConcurrentHashMap<String, Ventana> ventanasPorCliente = new ConcurrentHashMap<>();

	public TraductorRateLimiter(RateLimitProperties properties) {
		this.properties = properties;
	}

	public boolean permitir(String clienteId) {
		purgarVentanasVencidas();
		Ventana actualizada = ventanasPorCliente.compute(clienteId, this::avanzarVentana);
		return actualizada.conteo() <= properties.maxRequests();
	}

	private Ventana avanzarVentana(String clienteId, Ventana actual) {
		Instant ahora = Instant.now();
		if (actual == null || Duration.between(actual.inicio(), ahora).compareTo(properties.ventana()) >= 0) {
			return new Ventana(ahora, 1);
		}
		return new Ventana(actual.inicio(), actual.conteo() + 1);
	}

	/**
	 * Se ejecuta en cada request para que el mapa no crezca indefinidamente
	 * con IPs que ya no vuelven a pedir traducciones.
	 */
	private void purgarVentanasVencidas() {
		Instant limite = Instant.now().minus(properties.ventana());
		ventanasPorCliente.entrySet().removeIf(entrada -> entrada.getValue().inicio().isBefore(limite));
	}

	private record Ventana(Instant inicio, int conteo) {
	}
}
