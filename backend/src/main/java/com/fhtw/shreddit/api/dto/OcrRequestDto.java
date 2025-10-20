package com.fhtw.shreddit.api.dto;

public class OcrRequestDto {
    private Long documentId;
    private String documentTitle;

    // Constructors, getters, and setters
    public OcrRequestDto() {}

    public OcrRequestDto(Long documentId, String documentTitle) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }
}