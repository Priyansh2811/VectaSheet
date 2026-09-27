package com.vectasheet.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSpreadsheetRequest {
    @NotBlank(message = "Spreadsheet name is required")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
