package com.vectasheet.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCommentRequest {
    @NotBlank(message = "Comment can't be empty")
    private String body;

    private String quotedText;

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getQuotedText() { return quotedText; }
    public void setQuotedText(String quotedText) { this.quotedText = quotedText; }
}
