package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.dto.DecodeResultView;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView.IssueView;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView.PassengerView;
import com.xtremealex.aeroport.web.client.dto.InspectResultView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggrega i messaggi PAXLST decodificati per VOLO (chiave: numero volo + data partenza),
 * come previsto dai requirements ACDPDP (§2.2): più messaggi dello stesso volo
 * confluiscono in un'unica lista passeggeri.
 *
 * <p>Oltre alla lista passeggeri aggregata, ogni volo conserva i <b>singoli messaggi</b>
 * che lo compongono ({@link MessageRef}): questo permette alla vista di mostrare, nel
 * dettaglio del volo, i messaggi come "fogli" separati, ciascuno con il proprio
 * <b>ispettore</b> ({@link InspectResultView}) correlato per {@code messageId}.</p>
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

    /**
     * Un singolo messaggio PAXLST appartenente a un volo: i suoi passeggeri, le sue
     * anomalie e l'ispettore correlato (albero segmenti), quando disponibile.
     * È il "foglio strappato" mostrato nel dettaglio del volo.
     */
    record MessageRef(
            String messageId,
            String receivedAt,
            List<PassengerView> passengers,
            List<IssueView> issues,
            InspectResultView inspect,
            boolean valid) {

        public boolean hasInspect() {
            return inspect != null;
        }

        public boolean flagged() {
            if (issues == null) return false;
            for (IssueView i : issues) {
                String sev = i.severity() == null ? "" : i.severity().toUpperCase();
                if ("ERROR".equals(sev) || "WARNING".equals(sev) || "WARN".equals(sev)) return true;
            }
            return false;
        }
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
            boolean hasWarnings,
            String aircraftType,
            List<MessageRef> messages) {

        /** true se il volo ha almeno un'anomalia ERROR o WARNING da segnalare. */
        public boolean flagged() {
            return hasErrors || hasWarnings;
        }

        /** true se il tipo di aeromobile è disponibile (arricchimento da DB, quando presente). */
        public boolean hasAircraftType() {
            return aircraftType != null && !aircraftType.isBlank();
        }

        /** true se il volo è composto da più messaggi (mostra i "fogli" separati). */
        public boolean hasMultipleMessages() {
            return messages != null && messages.size() > 1;
        }
    }

    /**
     * Aggrega per volo correlando ogni messaggio decodificato con il rispettivo
     * ispettore. La correlazione avviene per {@code messageId}; se assente o non
     * univoco si ricade sull'ordine di apparizione (indice) — l'API restituisce
     * decode e inspect nello stesso ordine, una passata di parsing.
     */
    static List<FlightGroup> groupByFlight(List<DecodeResultView> results,
                                           List<InspectResultView> inspected) {
        if (results == null || results.isEmpty()) return List.of();

        Map<String, InspectResultView> inspectById = new LinkedHashMap<>();
        List<InspectResultView> inspectByOrder = inspected == null ? List.of() : inspected;
        for (InspectResultView ins : inspectByOrder) {
            if (ins.messageId() != null && !ins.messageId().isBlank()) {
                inspectById.putIfAbsent(ins.messageId(), ins);
            }
        }

        // preserva l'ordine di prima apparizione
        Map<String, Acc> byKey = new LinkedHashMap<>();
        int order = 0;
        for (DecodeResultView r : results) {
            if (r.message() == null) {
                order++;
                continue;
            }
            var m = r.message();
            String key = (m.flightNumber() == null ? "?" : m.flightNumber())
                    + "|" + (m.departureDateTime() == null ? "" : m.departureDateTime());
            Acc acc = byKey.computeIfAbsent(key, k -> new Acc(m.flightNumber(), m.carrier(),
                    m.departureAirport(), m.arrivalAirport(), m.departureDateTime()));
            acc.count++;

            List<PassengerView> pax = m.passengers() == null ? List.of() : m.passengers();
            if (!pax.isEmpty()) acc.passengers.addAll(pax);

            List<IssueView> msgIssues = r.issues() == null ? List.of() : r.issues();
            for (IssueView issue : msgIssues) {
                acc.issues.add(issue);
                String sev = issue.severity() == null ? "" : issue.severity().toUpperCase();
                if ("ERROR".equals(sev)) acc.hasErrors = true;
                else if ("WARNING".equals(sev) || "WARN".equals(sev)) acc.hasWarnings = true;
            }

            // correla l'ispettore: prima per messageId, poi per ordine
            InspectResultView ins = null;
            if (r.messageId() != null && inspectById.containsKey(r.messageId())) {
                ins = inspectById.get(r.messageId());
            } else if (order < inspectByOrder.size()) {
                ins = inspectByOrder.get(order);
            }

            acc.messages.add(new MessageRef(
                    r.messageId(), r.receivedAt(), pax, msgIssues, ins, r.valid()));
            order++;
        }

        List<FlightGroup> out = new ArrayList<>();
        for (Acc a : byKey.values()) {
            // aircraftType: arricchimento futuro da tabella aeromobili.
            // Il PAXLST NON trasporta il tipo di aeromobile, ma fornisce la ROTTA
            // (LOC partenza/arrivo) e il numero volo: sono la chiave di lookup naturale.
            // Oggi resta null finché non verrà popolata la tabella.
            String aircraftType = enrichAircraftType(a.flightNumber, a.departureAirport, a.arrivalAirport);
            out.add(new FlightGroup(a.flightNumber, a.carrier, a.departureAirport,
                    a.arrivalAirport, a.departureDateTime, a.count, a.passengers,
                    a.issues, a.hasErrors, a.hasWarnings, aircraftType, a.messages));
        }
        return out;
    }

    /**
     * Punto di arricchimento del tipo aeromobile. Lo standard PAXLST non lo contiene,
     * ma la ROTTA (aeroporto di partenza/arrivo, dai segmenti LOC) e il numero volo sono
     * disponibili e costituiscono la chiave di lookup naturale.
     *
     * <p>Oggi ritorna null. In futuro, quando sarà disponibile una tabella aeromobili
     * (es. flightNumber + rotta -> tipo di aeromobile), qui si aggancerà il lookup
     * (API o DB), tenendo la logica isolata invece che sparsa nei template.</p>
     */
    private static String enrichAircraftType(String flightNumber,
                                             String departureAirport, String arrivalAirport) {
        return null;
    }

    private static final class Acc {
        final String flightNumber, carrier, departureAirport, arrivalAirport, departureDateTime;
        final List<PassengerView> passengers = new ArrayList<>();
        final List<IssueView> issues = new ArrayList<>();
        final List<MessageRef> messages = new ArrayList<>();
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
