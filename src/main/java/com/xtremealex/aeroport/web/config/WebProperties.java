package com.xtremealex.aeroport.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà SEO/pubbliche dell'interfaccia web.
 *
 * <p>{@code publicBaseUrl} è il dominio pubblico di produzione usato per costruire
 * URL assoluti (canonical, hreflang, Open Graph, sitemap). È <b>configurabile</b>
 * via {@code aeroport.web.public-base-url} così da non hardcodare l'ambiente:
 * in locale può essere sovrascritto, in produzione vale il default.</p>
 *
 * <p>Il valore NON include il context-path (aggiunto a parte da server.servlet.context-path),
 * e viene normalizzato togliendo eventuale slash finale.</p>
 */
@ConfigurationProperties(prefix = "aeroport.web")
public record WebProperties(String publicBaseUrl) {

    public WebProperties {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "https://aeroport.bubume.it";
        }
        // normalizza: niente slash finale, così base + "/xtr-aeroport" resta pulito
        while (publicBaseUrl.endsWith("/")) {
            publicBaseUrl = publicBaseUrl.substring(0, publicBaseUrl.length() - 1);
        }
    }
}
