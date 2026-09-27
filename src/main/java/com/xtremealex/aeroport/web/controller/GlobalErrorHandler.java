package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.ApiBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.Locale;

/**
 * Gestione centralizzata degli errori applicativi.
 *
 * <p>Distingue errori di connessione (API giù) da errori applicativi (4xx) e,
 * soprattutto, garantisce un <b>catch-all</b>: qualsiasi eccezione non prevista
 * viene resa con la pagina {@code error-api} localizzata invece della Whitelabel
 * Error Page di Spring Boot ("no explicit mapping for /error"). Ogni errore
 * inatteso viene loggato con stacktrace per la diagnosi (il logging root è WARN,
 * quindi senza questo log gli errori resterebbero invisibili).</p>
 *
 * <p>I messaggi sono localizzati via {@link MessageSource} (chiavi {@code error.*}),
 * con fallback italiano se la chiave manca.</p>
 */
@ControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final MessageSource messages;

    public GlobalErrorHandler(MessageSource messages) {
        this.messages = messages;
    }

    /** File caricato troppo grande: risposta chiara invece di connessione chiusa. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String uploadTooLarge(MaxUploadSizeExceededException ex, Model model) {
        fill(model, "error.upload.title", "error.upload.body");
        return "error-api";
    }

    /** Solo problemi di rete/connessione: l'API non risponde. */
    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String apiUnavailable(ResourceAccessException ex, Model model) {
        log.warn("API non raggiungibile: {}", ex.getMessage());
        fill(model, "error.apiDown.title", "error.apiDown.body");
        return "error-api";
    }

    /** Richiesta non valida verso l'API (es. filtro non valido). */
    @ExceptionHandler(ApiBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(ApiBadRequestException ex, Model model) {
        model.addAttribute("title", msg("error.badRequest.title"));
        model.addAttribute("message", ex.getMessage());
        return "error-api";
    }

    /**
     * Input dell'utente non elaborabile: JSON malformato in fase di codifica,
     * lettura del file caricato fallita, ecc. Sono errori del client, non del server.
     */
    @ExceptionHandler({IOException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidInput(Exception ex, Model model) {
        log.warn("Input non valido: {}", ex.toString());
        fill(model, "error.invalidInput.title", "error.invalidInput.body");
        return "error-api";
    }

    /** Fallback per altri errori del client REST verso l'API. */
    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String restError(RestClientException ex, Model model) {
        log.warn("Errore di comunicazione con l'API: {}", ex.toString());
        fill(model, "error.comm.title", "error.comm.body");
        return "error-api";
    }

    /**
     * Catch-all: qualsiasi altra eccezione non prevista. Evita la Whitelabel Error
     * Page e mostra una pagina di errore localizzata con status 500. Logga lo
     * stacktrace completo per permettere la diagnosi.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String unexpected(Exception ex, Model model) {
        log.error("Errore inatteso non gestito", ex);
        fill(model, "error.unexpected.title", "error.unexpected.body");
        return "error-api";
    }

    private void fill(Model model, String titleKey, String bodyKey) {
        model.addAttribute("title", msg(titleKey));
        model.addAttribute("message", msg(bodyKey));
    }

    private String msg(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messages.getMessage(key, null, key, locale);
    }
}
