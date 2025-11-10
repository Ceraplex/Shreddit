package com.fhtw.shreddit.api.dto;

public class OcrFileRequestDto {
    private String bucket;
    private String objectName;

    public OcrFileRequestDto() {}

    public OcrFileRequestDto(String bucket, String objectName) {
        this.bucket = bucket;
        this.objectName = objectName;
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
}
