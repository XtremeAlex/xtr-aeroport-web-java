package com.xtremealex.aeroport.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Enumeration;

/**
 * Espone al layout le informazioni sulla richiesta corrente necessarie allo
 * switch di lingua, senza usare oggetti web nelle espressioni Thymeleaf
 * (in Thymeleaf 3.1 l'accesso diretto a #request è stato rimosso).
 *
 * <ul>
 *   <li>{@code langSwitchIt} / {@code langSwitchEn}: URL della pagina corrente
 *       con il parametro {@code lang} impostato, preservando path e filtri.</li>
 * </ul>
 */
@ControllerAdvice
public class LayoutModelAdvice {

    @ModelAttribute
    public void addLayoutAttributes(HttpServletRequest request, org.springframework.ui.Model model) {
        model.addAttribute("langSwitchIt", buildLangUrl(request, "it"));
        model.addAttribute("langSwitchEn", buildLangUrl(request, "en"));
    }

    /**
     * Ricostruisce l'URI della richiesta (comprensivo di context-path) con la
     * query string originale, sostituendo/aggiungendo il parametro {@code lang}.
     */
    private String buildLangUrl(HttpServletRequest request, String lang) {
        String uri = request.getRequestURI();
        StringBuilder query = new StringBuilder();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if ("lang".equals(name)) {
                continue;
            }
            for (String value : request.getParameterValues(name)) {
                query.append(query.isEmpty() ? "" : "&")
                        .append(encode(name)).append('=').append(encode(value));
            }
        }
        query.append(query.isEmpty() ? "" : "&").append("lang=").append(lang);
        return uri + "?" + query;
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
