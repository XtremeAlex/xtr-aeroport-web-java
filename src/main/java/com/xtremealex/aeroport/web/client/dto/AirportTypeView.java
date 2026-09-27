package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportTypeView(Long id, String name, String description) {
}
