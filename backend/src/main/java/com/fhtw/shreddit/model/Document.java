package com.fhtw.shreddit.model;

/**
 * Simple DTO mirroring the OpenAPI Document schema.
 * Added to decouple the build from OpenAPI codegen so Maven can compile reliably.
 */
public class Document {
    private Long id;
    private String title;
    private String content;

    public Document() {}

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
