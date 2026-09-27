package com.xtremealex.aeroport.web.client;

import com.xtremealex.aeroport.web.client.dto.AirportTypeView;
import com.xtremealex.aeroport.web.client.dto.AirportView;
import com.xtremealex.aeroport.web.client.dto.CountryView;
import com.xtremealex.aeroport.web.client.dto.PageResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

/** Unico punto di accesso alle API di xtr-aeroport-api. Nessuna logica di dominio. */
@Component
public class AeroportApiClient {

    private static final ParameterizedTypeReference<PageResponse<AirportView>> AIRPORT_PAGE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<PageResponse<CountryView>> COUNTRY_PAGE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<PageResponse<AirportTypeView>> TYPE_PAGE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public AeroportApiClient(RestClient aeroportRestClient) {
        this.client = aeroportRestClient;
    }

    public PageResponse<AirportView> searchAirports(String name, String isoCountry, int page, int size) {
        return client.get()
                .uri(uri -> buildAirportUri(uri, name, isoCountry, page, size))
                .retrieve()
                .body(AIRPORT_PAGE);
    }

    private java.net.URI buildAirportUri(UriBuilder uri, String name, String isoCountry, int page, int size) {
        uri.path("/api/v1/airports").queryParam("page", page).queryParam("size", size);
        if (name != null && !name.isBlank()) uri.queryParam("name", name);
        if (isoCountry != null && !isoCountry.isBlank()) uri.queryParam("isoCountry", isoCountry);
        return uri.build();
    }

    public PageResponse<CountryView> countries(int page, int size) {
        return client.get()
                .uri(uri -> uri.path("/api/v1/countries")
                        .queryParam("page", page).queryParam("size", size).build())
                .retrieve()
                .body(COUNTRY_PAGE);
    }

    public PageResponse<AirportTypeView> airportTypes(int page, int size) {
        return client.get()
                .uri(uri -> uri.path("/api/v1/airport-types")
                        .queryParam("page", page).queryParam("size", size).build())
                .retrieve()
                .body(TYPE_PAGE);
    }
}
