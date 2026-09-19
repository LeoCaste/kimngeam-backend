package com.kimngeam.backend.traductor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Umbral de similitud coseno bajo el cual un chunk recuperado se descarta
 * como material de referencia — meter contexto irrelevante confunde al
 * modelo (ver CLAUDE.md, Fase 3). Default 0.5 como punto de partida
 * razonable; conviene recalibrarlo empíricamente contra el corpus real, como
 * se hizo con el modelo de embeddings en Fase 2.
 */
@ConfigurationProperties(prefix = "kimngeam.traductor")
public record TraductorProperties(@DefaultValue("0.5") double umbralSimilitud) {
}
