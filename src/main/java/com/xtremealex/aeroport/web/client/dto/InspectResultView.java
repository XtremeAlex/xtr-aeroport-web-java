package com.xtremealex.aeroport.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InspectResultView(
        String receivedAt, String messageId,
        Delimiters delimiters, List<SegmentNode> segments,
        List<DecodeResultView.IssueView> issues, boolean valid) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Delimiters(String component, String element, String decimal,
                             String release, String segment) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SegmentNode(int index, String tag, String description,
                              String usage, boolean known, List<ElementNode> elements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ElementNode(int position, List<String> components) {
    }
}
