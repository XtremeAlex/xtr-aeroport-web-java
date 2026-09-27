package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.AeroportApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CountryController {

    private final AeroportApi api;

    public CountryController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/countries")
    public String countries(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        var result = api.countries(page, size);
        model.addAttribute("countries", result.contentOrEmpty());
        model.addAttribute("page", result);
        model.addAttribute("title", "Paesi");
        return "countries";
    }
}
