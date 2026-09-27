package com.vectasheet.dto;

public class UpdateDocumentRequest {
    private String title;
    private String contentHtml;
    private long expectedVersion = -1;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public long getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(long expectedVersion) { this.expectedVersion = expectedVersion; }
}
