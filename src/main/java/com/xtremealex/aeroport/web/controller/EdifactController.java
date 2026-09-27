package com.xtremealex.aeroport.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremealex.aeroport.web.client.AeroportApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Pagina di conversione EDIFACT (tabellone aeroporto): decode multi-messaggio e encode. */
@Controller
public class EdifactController {

    private final AeroportApi api;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Oltre questi limiti la vista tronca per restare leggera (il browser non regge
     *  decine di migliaia di righe). L'archivio EDIFACT dedicato resta la via per i big file. */
    private static final int MAX_FLIGHTS_SHOWN = 200;
    private static final int MAX_INSPECT_SHOWN = 50;
    /** Soglia oltre cui avvisare che è preferibile l'archivio (~2MB). */
    private static final int LARGE_INPUT_BYTES = 2 * 1024 * 1024;
    /** HARD STOP della vista sincrona: oltre questa dimensione NON si chiama l'API
     *  (il decode in-memory di file enormi la manderebbe in OutOfMemory). Si invita
     *  all'archivio EDIFACT, che processa a lotti in streaming. */
    private static final int MAX_SYNC_INPUT_BYTES = 5 * 1024 * 1024;

    public EdifactController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/edifact")
    public String page(Model model) {
        return "edifact";
    }

    @PostMapping("/edifact/decode")
    public String decode(@RequestParam(value = "raw", required = false) String raw,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Model model) throws IOException {
        String content = raw;
        if (file != null && !file.isEmpty()) {
            // Legge lo stream direttamente in testo: evita la doppia allocazione
            // byte[] (getBytes) + String, che su heap piccolo causava OutOfMemoryError.
            content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        model.addAttribute("rawInput", raw);

        if (content == null || content.isBlank()) {
            model.addAttribute("flights", List.of());
            model.addAttribute("inspect", List.of());
            return "edifact";
        }

        int inputBytes = content.length();

        // HARD STOP: file troppo grande per la vista sincrona. Non chiamiamo l'API
        // (la proteggiamo dall'OutOfMemory): mostriamo l'invito all'archivio.
        if (inputBytes > MAX_SYNC_INPUT_BYTES) {
            model.addAttribute("tooBigForSync", true);
            model.addAttribute("largeInput", true);
            model.addAttribute("flights", List.of());
            model.addAttribute("inspect", List.of());
            return "edifact";
        }

        // avviso "file grande": la vista sincrona resta ma suggeriamo l'archivio
        model.addAttribute("largeInput", inputBytes > LARGE_INPUT_BYTES);

        // UNA sola chiamata all'API: decode + inspect in una passata (niente doppio parsing).
        var analysis = api.analyzeEdifact(content);
        var allInspect = analysis.inspectedOrEmpty();
        // aggrega per volo, correlando ogni messaggio col suo ispettore (per messageId/ordine)
        var allFlights = EdifactAggregator.groupByFlight(analysis.decodedOrEmpty(), allInspect);

        // CAP di rendering: mostriamo un sottoinsieme, comunicando il totale.
        int totalFlights = allFlights.size();
        int totalInspect = allInspect.size();
        var flightsShown = totalFlights > MAX_FLIGHTS_SHOWN
                ? allFlights.subList(0, MAX_FLIGHTS_SHOWN) : allFlights;
        var inspectShown = totalInspect > MAX_INSPECT_SHOWN
                ? allInspect.subList(0, MAX_INSPECT_SHOWN) : allInspect;

        model.addAttribute("flights", flightsShown);
        model.addAttribute("inspect", inspectShown);
        model.addAttribute("totalFlights", totalFlights);
        model.addAttribute("totalInspect", totalInspect);
        model.addAttribute("flightsTruncated", totalFlights > MAX_FLIGHTS_SHOWN);
        model.addAttribute("inspectTruncated", totalInspect > MAX_INSPECT_SHOWN);
        model.addAttribute("shownFlights", flightsShown.size());
        return "edifact";
    }

    @PostMapping("/edifact/encode")
    public String encode(@RequestParam("json") String json, Model model) throws IOException {
        model.addAttribute("jsonInput", json);
        Map<String, Object> payload = objectMapper.readValue(json, new TypeReference<>() {});
        model.addAttribute("encoded", api.encodeEdifact(payload));
        return "edifact";
    }
}
