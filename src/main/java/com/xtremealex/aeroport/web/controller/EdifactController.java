package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.AeroportApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Pagina di conversione EDIFACT (tabellone aeroporto): decode e encode. */
@Controller
public class EdifactController {

    private final AeroportApi api;

    public EdifactController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/edifact")
    public String page(Model model) {
        model.addAttribute("title", "EDIFACT · Convertitore PAXLST");
        return "edifact";
    }

    @PostMapping("/edifact/decode")
    public String decode(@RequestParam("raw") String raw, Model model) {
        model.addAttribute("title", "EDIFACT · Decodifica");
        model.addAttribute("rawInput", raw);
        model.addAttribute("results", api.decodeEdifact(raw));
        return "edifact";
    }

    @PostMapping("/edifact/encode")
    public String encode(@RequestParam("json") String json, Model model) {
        model.addAttribute("title", "EDIFACT · Compilazione");
        model.addAttribute("jsonInput", json);
        model.addAttribute("encoded", api.encodeEdifact(json));
        return "edifact";
    }
}
