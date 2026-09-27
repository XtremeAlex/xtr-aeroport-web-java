package com.xtremealex.aeroport.web.client;

import com.xtremealex.aeroport.web.client.dto.AirportTypeView;
import com.xtremealex.aeroport.web.client.dto.AirportView;
import com.xtremealex.aeroport.web.client.dto.CountryView;
import com.xtremealex.aeroport.web.client.dto.PageResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

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
}
