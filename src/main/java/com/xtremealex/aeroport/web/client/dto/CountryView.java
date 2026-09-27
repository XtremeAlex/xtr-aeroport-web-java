package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryView(
        Long id,
        String isoAlpha2,
        String isoAlpha3,
        String name,
        String description
) {
}
