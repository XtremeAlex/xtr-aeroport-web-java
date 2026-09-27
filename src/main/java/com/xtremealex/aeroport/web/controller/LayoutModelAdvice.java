package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.config.WebProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Enumeration;
import java.util.Locale;

/**
 * Espone al layout le informazioni sulla richiesta corrente necessarie allo
 * switch di lingua e al SEO tecnico (canonical, hreflang, Open Graph), senza
 * usare oggetti web nelle espressioni Thymeleaf (in Thymeleaf 3.1 l'accesso
 * diretto a #request è stato rimosso).
 *
 * <ul>
 *   <li>{@code langSwitchIt} / {@code langSwitchEn}: URL <i>relativi</i> della pagina
 *       corrente con il parametro {@code lang} impostato (per lo switch nel nav).</li>
 *   <li>{@code canonicalUrl}: URL <b>assoluto</b> della pagina corrente (base pubblico
 *       configurabile + path), senza il parametro {@code lang} (evita duplicati SEO).</li>
 *   <li>{@code urlIt} / {@code urlEn}: URL assoluti della stessa pagina con
 *       {@code ?lang=it|en}, per i {@code <link rel="alternate" hreflang>}.</li>
 *   <li>{@code ogLocale} / {@code ogLocaleAlt}: locale Open Graph (it_IT / en_US).</li>
 * </ul>
 *
 * <p>Il base URL pubblico è configurabile ({@link WebProperties}); il path corrente
 * (comprensivo di context-path) viene letto dalla richiesta. Nessun localhost è
 * hardcodato: in produzione vale {@code https://aeroport.bubume.it}.</p>
 */
@ControllerAdvice
public class LayoutModelAdvice {

    private final WebProperties webProperties;
    private final MessageSource messages;

    public LayoutModelAdvice(WebProperties webProperties, MessageSource messages) {
        this.webProperties = webProperties;
        this.messages = messages;
    }

    @ModelAttribute
    public void addLayoutAttributes(HttpServletRequest request, org.springframework.ui.Model model) {
        // Switch lingua (relativo): preserva path + filtri, imposta ?lang
        model.addAttribute("langSwitchIt", buildLangUrl(request, "it"));
        model.addAttribute("langSwitchEn", buildLangUrl(request, "en"));

        // Path corrente comprensivo di context-path (es. /xtr-aeroport/edifact)
        String path = request.getRequestURI();
        String base = webProperties.publicBaseUrl();
        String canonical = base + path;

        // Canonical: assoluto, senza query (nessun ?lang, niente parametri di filtro/pagina)
        model.addAttribute("canonicalUrl", canonical);

        // Alternate hreflang: stessa pagina forzando la lingua
        model.addAttribute("urlIt", canonical + "?lang=it");
        model.addAttribute("urlEn", canonical + "?lang=en");

        // Open Graph locale in base alla lingua attiva
        Locale locale = LocaleContextHolder.getLocale();
        boolean it = "it".equals(locale.getLanguage());
        model.addAttribute("ogLocale", it ? "it_IT" : "en_US");
        model.addAttribute("ogLocaleAlt", it ? "en_US" : "it_IT");

        // JSON-LD costruito server-side (stampato con th:utext): evita problemi di
        // parsing dei blocchi <script> in Thymeleaf 3.1 (niente th:inline nel template).
        String appDesc = jsonEscape(messages.getMessage("seo.jsonld.appDesc", null, "", locale));
        model.addAttribute("jsonLdApp", buildAppJsonLd(canonical, appDesc));
        model.addAttribute("jsonLdHome", buildHomeJsonLd(canonical, appDesc));
    }

    private String buildAppJsonLd(String url, String desc) {
        String u = jsonEscape(url);
        return "{"
                + "\"@context\":\"https://schema.org\","
                + "\"@type\":\"SoftwareApplication\","
                + "\"name\":\"Aeroport\","
                + "\"applicationCategory\":\"BusinessApplication\","
                + "\"operatingSystem\":\"Web\","
                + "\"url\":\"" + u + "\","
                + "\"description\":\"" + desc + "\","
                + "\"isAccessibleForFree\":true,"
                + "\"offers\":{\"@type\":\"Offer\",\"price\":\"0\",\"priceCurrency\":\"EUR\"},"
                + "\"author\":{\"@type\":\"Person\",\"name\":\"Andrei Alexandru Dabija\","
                + "\"alternateName\":\"XtremeAlex\",\"url\":\"https://2ad.bubume.it\"}"
                + "}";
    }

    private String buildHomeJsonLd(String url, String desc) {
        String u = jsonEscape(url);
        return "{"
                + "\"@context\":\"https://schema.org\","
                + "\"@type\":\"WebSite\","
                + "\"name\":\"Aeroport\","
                + "\"url\":\"" + u + "\","
                + "\"inLanguage\":[\"it\",\"en\"],"
                + "\"description\":\"" + desc + "\","
                + "\"publisher\":{\"@type\":\"Organization\",\"name\":\"Aeroport\",\"url\":\"" + u + "\","
                + "\"founder\":{\"@type\":\"Person\",\"name\":\"Andrei Alexandru Dabija\","
                + "\"alternateName\":\"XtremeAlex\",\"url\":\"https://2ad.bubume.it\"}}"
                + "}";
    }

    /** Escape minimale per inserire testo in una stringa JSON. */
    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e");
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
