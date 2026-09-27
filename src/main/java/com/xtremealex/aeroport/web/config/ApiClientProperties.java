package com.xtremealex.aeroport.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configurazione del client verso xtr-aeroport-api. */
@ConfigurationProperties(prefix = "aeroport.api")
public record ApiClientProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public ApiClientProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080/xtr-aeroport-api";
        }
        if (connectTimeoutMs <= 0) connectTimeoutMs = 2000;
        if (readTimeoutMs <= 0) readTimeoutMs = 5000;
    }
}
