package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportView(
        Long id,
        String iataCode,
        String icaoCode,
        String name,
        String municipality,
        String isoCountry,
        AirportTypeView airportType
) {
}
