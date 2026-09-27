package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.config.WebProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sitemap XML dinamica servita su {@code GET /sitemap.xml}.
 *
 * <p>Contiene solo le <b>landing pubbliche GET</b> (home, decodifica, creazione,
 * aeroporti, paesi). Le pagine con risultati di decodifica (POST) NON vanno in
 * sitemap: non sono URL stabili e non vanno indicizzate.</p>
 *
 * <p>Ogni URL include gli alternate {@code <xhtml:link rel="alternate" hreflang>}
 * per it/en/x-default, coerenti con i canonical/hreflang delle pagine. Gli URL
 * assoluti usano il base pubblico configurabile ({@link WebProperties}) + il
 * context-path dell'applicazione (nessun localhost hardcodato).</p>
 */
@RestController
public class SitemapController {

    /** Path pubblici (relativi al context-path) da includere in sitemap. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/", "/edifact", "/edifact/new", "/airports", "/countries");

    private final String baseUrl;
    private final String contextPath;

    public SitemapController(WebProperties webProperties,
                             @Value("${server.servlet.context-path:}") String contextPath) {
        this.baseUrl = webProperties.publicBaseUrl();
        // normalizza il context-path: senza slash finale ("" oppure "/xtr-aeroport")
        String ctx = contextPath == null ? "" : contextPath.trim();
        if (ctx.endsWith("/")) {
            ctx = ctx.substring(0, ctx.length() - 1);
        }
        this.contextPath = ctx;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ")
           .append("xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");

        for (String path : PUBLIC_PATHS) {
            String loc = absolute(path);
            String it = loc + "?lang=it";
            String en = loc + "?lang=en";
            xml.append("  <url>\n");
            xml.append("    <loc>").append(escape(loc)).append("</loc>\n");
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"it\" href=\"")
               .append(escape(it)).append("\"/>\n");
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"en\" href=\"")
               .append(escape(en)).append("\"/>\n");
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
               .append(escape(loc)).append("\"/>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>\n");
        return xml.toString();
    }

    /** base + context-path + path, evitando doppi slash sulla root. */
    private String absolute(String path) {
        if ("/".equals(path)) {
            return baseUrl + contextPath + "/";
        }
        return baseUrl + contextPath + path;
    }

    /** Escape XML minimale per i caratteri riservati nelle URL (es. &amp;). */
    private String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
