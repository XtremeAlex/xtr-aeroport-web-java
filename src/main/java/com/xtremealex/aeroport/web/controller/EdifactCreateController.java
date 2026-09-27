package com.xtremealex.aeroport.web.controller;

import com.xtremealex.aeroport.web.client.AeroportApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Form di composizione di un messaggio EDIFACT PAXLST da zero. */
@Controller
public class EdifactCreateController {

    private final AeroportApi api;

    public EdifactCreateController(AeroportApi api) {
        this.api = api;
    }

    @GetMapping("/edifact/new")
    public String form(Model model) {
        model.addAttribute("title", "Crea EDIFACT · PAXLST");
        return "edifact-new";
    }

    /** Riceve i campi del form (volo + array passeggeri) e genera il messaggio EDIFACT. */
    @PostMapping("/edifact/create")
    public String create(@RequestParam Map<String, String> form, Model model) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("messageFunction", orDefault(form.get("messageFunction"), "745"));
        msg.put("flightNumber", form.get("flightNumber"));
        msg.put("carrier", form.get("carrier"));
        msg.put("departureAirport", upper(form.get("departureAirport")));
        msg.put("arrivalAirport", upper(form.get("arrivalAirport")));
        msg.put("departureDateTime", form.get("departureDateTime"));
        msg.put("arrivalDate", form.get("arrivalDate"));
        msg.put("reportingParty", form.get("reportingParty"));

        // passeggeri: campi pax[i].campo
        List<Map<String, Object>> passengers = new ArrayList<>();
        int i = 0;
        while (form.containsKey("pax[" + i + "].surname")) {
            String surname = form.get("pax[" + i + "].surname");
            if (surname != null && !surname.isBlank()) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("type", orDefault(form.get("pax[" + i + "].type"), "FL"));
                p.put("surname", upper(surname));
                p.put("givenName", upper(form.get("pax[" + i + "].givenName")));
                p.put("gender", form.get("pax[" + i + "].gender"));
                p.put("nationality", upper(form.get("pax[" + i + "].nationality")));
                p.put("birthDate", form.get("pax[" + i + "].birthDate"));
                p.put("pnr", form.get("pax[" + i + "].pnr"));
                Map<String, Object> doc = new LinkedHashMap<>();
                doc.put("type", orDefault(form.get("pax[" + i + "].docType"), "P"));
                doc.put("number", form.get("pax[" + i + "].docNumber"));
                doc.put("expiry", form.get("pax[" + i + "].docExpiry"));
                doc.put("issuingCountry", upper(form.get("pax[" + i + "].docCountry")));
                p.put("documents", List.of(doc));
                passengers.add(p);
            }
            i++;
        }
        msg.put("passengers", passengers);

        model.addAttribute("title", "Crea EDIFACT · PAXLST");
        model.addAttribute("encoded", api.encodeEdifact(msg));
        model.addAttribute("submitted", msg);
        return "edifact-new";
    }

    private String upper(String s) { return s == null ? null : s.trim().toUpperCase(); }
    private String orDefault(String s, String d) { return s == null || s.isBlank() ? d : s; }
}
