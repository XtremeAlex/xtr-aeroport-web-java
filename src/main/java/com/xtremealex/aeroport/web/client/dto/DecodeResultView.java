package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DecodeResultView(String receivedAt, String messageId,
                               PaxlstView message, List<IssueView> issues, boolean valid) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaxlstView(
            String messageFunction, String flightNumber, String carrier,
            String departureAirport, String arrivalAirport,
            String departureDateTime, String arrivalDate, String reportingParty,
            List<PassengerView> passengers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PassengerView(
            String type, String surname, String givenName, String gender,
            String nationality, String birthDate,
            String documentType, String documentNumber, String documentExpiry) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueView(String severity, String code, String message, String segmentTag) {
    }
}
