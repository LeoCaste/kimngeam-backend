package com.kimngeam.backend.traductor;

/**
 * Un aporte cultural ya persistido, en el shape que espera el contrato de API
 * (ver {@code docs/API_CONTRACTS.md}, {@code POST /traductor/traducir}).
 */
public record ContextoCulturalResultado(String expresion, String aporte, String comunidad, String variante) {
}
