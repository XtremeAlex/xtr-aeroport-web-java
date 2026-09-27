package com.xtremealex.aeroport.web.client.dto;

import java.util.List;

/** Mappa la risposta paginata di xtr-aeroport-api. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public List<T> contentOrEmpty() {
        return content == null ? List.of() : content;
    }
}
