package com.fhtw.genaiworker.dto;

public class GenAiRequestDto {
    private Long documentId;
    private String ocrPath; // MinIO object name of OCR text (e.g., "7/ocr.txt" or "documents/7/ocr.txt")

    public GenAiRequestDto() {}

    public GenAiRequestDto(Long documentId, String ocrPath) {
        this.documentId = documentId;
        this.ocrPath = ocrPath;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getOcrPath() {
        return ocrPath;
    }

    public void setOcrPath(String ocrPath) {
        this.ocrPath = ocrPath;
    }
}
