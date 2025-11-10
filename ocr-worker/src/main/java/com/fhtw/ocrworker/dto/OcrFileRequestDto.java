package com.fhtw.ocrworker.dto;

public class OcrFileRequestDto {
    private String bucket;
    private String objectName;
    private String username;

    public OcrFileRequestDto() {}

    public OcrFileRequestDto(String bucket, String objectName, String username) {
        this.bucket = bucket;
        this.objectName = objectName;
        this.username = username;
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
