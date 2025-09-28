package com.fhtw.shreddit.api.dto;

/**
 * Pure API DTO used for JSON serialization/deserialization.
 * Decoupled from JPA to avoid any persistence annotations affecting Jackson binding.
 */
public class DocumentDto {
    private Long id;
    private String title;
    private String content;

    public DocumentDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
