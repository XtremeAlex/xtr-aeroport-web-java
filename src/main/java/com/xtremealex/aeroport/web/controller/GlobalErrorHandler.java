package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.ApiBadRequestException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Distingue errori di connessione (API giù) da errori applicativi (4xx). */
@ControllerAdvice
public class GlobalErrorHandler {

    /** File caricato troppo grande: risposta chiara invece di connessione chiusa. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String uploadTooLarge(MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("title", "File troppo grande");
        model.addAttribute("message",
                "Il file supera la dimensione massima consentita per l'upload. "
                + "Riduci il file oppure usa il servizio di archivio EDIFACT dedicato per i file di grandi dimensioni.");
        return "error-api";
    }

    /** Solo problemi di rete/connessione: l'API non risponde. */
    @ExceptionHandler(ResourceAccessException.class)
    public String apiUnavailable(ResourceAccessException ex, Model model) {
        model.addAttribute("title", "Servizio non disponibile");
        model.addAttribute("message", "Impossibile contattare xtr-aeroport-api. Riprovare più tardi.");
        return "error-api";
    }

    /** Richiesta non valida verso l'API (es. filtro non valido). */
    @ExceptionHandler(ApiBadRequestException.class)
    public String badRequest(ApiBadRequestException ex, Model model) {
        model.addAttribute("title", "Richiesta non valida");
        model.addAttribute("message", ex.getMessage());
        return "error-api";
    }

    /** Fallback per altri errori del client REST. */
    @ExceptionHandler(RestClientException.class)
    public String restError(RestClientException ex, Model model) {
        model.addAttribute("title", "Errore di comunicazione");
        model.addAttribute("message", "Errore nella comunicazione con l'API.");
        return "error-api";
    }
}
