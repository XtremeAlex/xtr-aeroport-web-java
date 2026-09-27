package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Risultato combinato decode + inspect ottenuto con una sola chiamata all'API
 * (endpoint /edifact/analyze): evita la doppia richiesta e il doppio parsing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyzeResultView(
        List<DecodeResultView> decoded,
        List<InspectResultView> inspected
) {
    public List<DecodeResultView> decodedOrEmpty() {
        return decoded == null ? List.of() : decoded;
    }

    public List<InspectResultView> inspectedOrEmpty() {
        return inspected == null ? List.of() : inspected;
    }
}
