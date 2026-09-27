package com.xtremealex.aeroport.web.client;

/** L'API ha risposto con 4xx: richiesta non valida (non è un problema di connessione). */
public class ApiBadRequestException extends RuntimeException {
    public ApiBadRequestException(String message) {
        super(message);
    }
}
