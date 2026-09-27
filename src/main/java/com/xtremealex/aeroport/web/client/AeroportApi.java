package com.xtremealex.aeroport.web.client;

import com.xtremealex.aeroport.web.client.dto.AirportTypeView;
import com.xtremealex.aeroport.web.client.dto.AirportView;
import com.xtremealex.aeroport.web.client.dto.CountryView;
import com.xtremealex.aeroport.web.client.dto.DecodeResultView;
import com.xtremealex.aeroport.web.client.dto.PageResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * Client dichiarativo (stile Feign, nativo Spring 6) verso xtr-aeroport-api.
 * Nessuna logica: solo il contratto HTTP. L'implementazione è generata da
 * HttpServiceProxyFactory (vedi RestClientConfig).
 */
@HttpExchange(url = "/api/v1", accept = "application/json")
public interface AeroportApi {

    @GetExchange("/airports")
    PageResponse<AirportView> searchAirports(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String isoCountry,
            @RequestParam int page,
            @RequestParam int size);

    @GetExchange("/countries")
    PageResponse<CountryView> countries(@RequestParam int page, @RequestParam int size);

    @GetExchange("/airport-types")
    PageResponse<AirportTypeView> airportTypes(@RequestParam int page, @RequestParam int size);

    /** Decodifica un messaggio EDIFACT (text/plain) -> lista di risultati con validazione. */
    @PostExchange(url = "/edifact/decode", contentType = "text/plain")
    List<DecodeResultView> decodeEdifact(@RequestBody String raw);

    /** Compila un messaggio EDIFACT PAXLST a partire dal modello di dominio (mappa JSON). */
    @PostExchange(url = "/edifact/encode", contentType = "application/json", accept = "text/plain")
    String encodeEdifact(@RequestBody java.util.Map<String, Object> paxlst);
}
