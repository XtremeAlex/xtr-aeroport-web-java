package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.dto.DecodeResultView;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView.IssueView;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView.PassengerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggrega i messaggi PAXLST decodificati per VOLO (chiave: numero volo + data partenza),
 * come previsto dai requirements ACDPDP (§2.2): più messaggi dello stesso volo
 * confluiscono in un'unica lista passeggeri.
 *
 * <p>Le anomalie di validazione ({@link IssueView}) arrivano a livello di messaggio,
 * non del singolo passeggero: lo standard PAXLST non fornisce una correlazione precisa
 * issue&nbsp;→&nbsp;passeggero. Il gruppo di volo raccoglie quindi tutte le issue dei
 * suoi messaggi e i flag {@code hasErrors}/{@code hasWarnings}, così la vista può
 * segnalare l'intero volo (e i relativi passeggeri) come sospetto.</p>
 */
final class EdifactAggregator {

    private EdifactAggregator() {
    }

    record FlightGroup(
            String flightNumber,
            String carrier,
            String departureAirport,
            String arrivalAirport,
            String departureDateTime,
            int messageCount,
            List<PassengerView> passengers,
            List<IssueView> issues,
            boolean hasErrors,
            boolean hasWarnings) {

        /** true se il volo ha almeno un'anomalia ERROR o WARNING da segnalare. */
        public boolean flagged() {
            return hasErrors || hasWarnings;
        }
    }

    static List<FlightGroup> groupByFlight(List<DecodeResultView> results) {
        if (results == null || results.isEmpty()) return List.of();

        // preserva l'ordine di prima apparizione
        Map<String, Acc> byKey = new LinkedHashMap<>();
        for (DecodeResultView r : results) {
            if (r.message() == null) continue;
            var m = r.message();
            String key = (m.flightNumber() == null ? "?" : m.flightNumber())
                    + "|" + (m.departureDateTime() == null ? "" : m.departureDateTime());
            Acc acc = byKey.computeIfAbsent(key, k -> new Acc(m.flightNumber(), m.carrier(),
                    m.departureAirport(), m.arrivalAirport(), m.departureDateTime()));
            acc.count++;
            if (m.passengers() != null) acc.passengers.addAll(m.passengers());
            if (r.issues() != null) {
                for (IssueView issue : r.issues()) {
                    acc.issues.add(issue);
                    String sev = issue.severity() == null ? "" : issue.severity().toUpperCase();
                    if ("ERROR".equals(sev)) acc.hasErrors = true;
                    else if ("WARNING".equals(sev) || "WARN".equals(sev)) acc.hasWarnings = true;
                }
            }
        }

        List<FlightGroup> out = new ArrayList<>();
        for (Acc a : byKey.values()) {
            out.add(new FlightGroup(a.flightNumber, a.carrier, a.departureAirport,
                    a.arrivalAirport, a.departureDateTime, a.count, a.passengers,
                    a.issues, a.hasErrors, a.hasWarnings));
        }
        return out;
    }

    private static final class Acc {
        final String flightNumber, carrier, departureAirport, arrivalAirport, departureDateTime;
        final List<PassengerView> passengers = new ArrayList<>();
        final List<IssueView> issues = new ArrayList<>();
        int count;
        boolean hasErrors;
        boolean hasWarnings;

        Acc(String fn, String c, String dep, String arr, String dt) {
            this.flightNumber = fn;
            this.carrier = c;
            this.departureAirport = dep;
            this.arrivalAirport = arr;
            this.departureDateTime = dt;
        }
    }
}
