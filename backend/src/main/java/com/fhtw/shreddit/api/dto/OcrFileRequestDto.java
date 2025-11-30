package com.fhtw.shreddit.api.dto;

public class OcrFileRequestDto {
    private Long documentId;
    private String bucket;
    private String objectName;
    private String username;

    public OcrFileRequestDto() {}

    public OcrFileRequestDto(Long documentId, String bucket, String objectName, String username) {
        this.documentId = documentId;
        this.bucket = bucket;
        this.objectName = objectName;
        this.username = username;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
