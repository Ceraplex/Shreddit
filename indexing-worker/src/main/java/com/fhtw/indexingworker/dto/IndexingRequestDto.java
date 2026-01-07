package com.fhtw.indexingworker.dto;

public class IndexingRequestDto {
    private Long documentId;
    private String bucket;
    private String objectName;

    public IndexingRequestDto() {
    }

    public IndexingRequestDto(Long documentId, String bucket, String objectName) {
        this.documentId = documentId;
        this.bucket = bucket;
        this.objectName = objectName;
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
}
