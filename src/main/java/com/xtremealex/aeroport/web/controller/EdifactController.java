package com.xtremealex.aeroport.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremealex.aeroport.web.client.AeroportApi;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView;
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

    public EdifactController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/edifact")
    public String page(Model model) {
        model.addAttribute("title", "EDIFACT · Convertitore PAXLST");
        return "edifact";
    }

    @PostMapping("/edifact/decode")
    public String decode(@RequestParam(value = "raw", required = false) String raw,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Model model) throws IOException {
        String content = raw;
        if (file != null && !file.isEmpty()) {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        model.addAttribute("title", "EDIFACT · Decodifica");
        model.addAttribute("rawInput", raw);
        List<DecodeResultView> results = (content == null || content.isBlank())
                ? List.of() : api.decodeEdifact(content);
        model.addAttribute("results", results);
        // aggregazione per volo (numero volo) preservando l'ordine di arrivo
        model.addAttribute("flights", EdifactAggregator.groupByFlight(results));
        // ispettore ad albero (segmenti/elementi) stile EDI inspector
        model.addAttribute("inspect", (content == null || content.isBlank())
                ? List.of() : api.inspectEdifact(content));
        return "edifact";
    }

    @PostMapping("/edifact/encode")
    public String encode(@RequestParam("json") String json, Model model) throws IOException {
        model.addAttribute("title", "EDIFACT · Compilazione");
        model.addAttribute("jsonInput", json);
        Map<String, Object> payload = objectMapper.readValue(json, new TypeReference<>() {});
        model.addAttribute("encoded", api.encodeEdifact(payload));
        return "edifact";
    }
}
