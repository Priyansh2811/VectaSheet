package com.vectasheet.dto;

import java.util.UUID;

public class SearchResultDto {
    private String type;
    private UUID id;
    private String title;
    private String snippet;

    public SearchResultDto(String type, UUID id, String title, String snippet) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.snippet = snippet;
    }

    public String getType() { return type; }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSnippet() { return snippet; }
}
