package com.xtremealex.aeroport.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home / vetrina del prodotto.
 *
 * Presenta le due funzioni principali (decodifica e creazione di messaggi
 * EDIFACT PAXLST) e, come dati di riferimento secondari, aeroporti e paesi.
 * Il titolo di pagina e tutti i testi sono localizzati dai template via #{...}.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}
