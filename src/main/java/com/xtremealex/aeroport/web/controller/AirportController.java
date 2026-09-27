package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.AeroportApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AirportController {

    private final AeroportApi api;

    public AirportController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/airports")
    public String airports(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String isoCountry,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           Model model) {
        var result = api.searchAirports(name, isoCountry, page, size);
        model.addAttribute("airports", result.contentOrEmpty());
        model.addAttribute("page", result);
        model.addAttribute("filterName", name);
        model.addAttribute("filterCountry", isoCountry);
        return "airports";
    }
}
