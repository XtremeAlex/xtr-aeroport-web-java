package com.xtremealex.aeroport.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

/** Se l'API interna non è raggiungibile, mostra una pagina d'errore chiara (niente stacktrace). */
@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(RestClientException.class)
    public String apiUnavailable(RestClientException ex, Model model) {
        model.addAttribute("title", "Servizio non disponibile");
        model.addAttribute("message", "Impossibile contattare xtr-aeroport-api. Riprovare più tardi.");
        return "error-api";
    }
}
