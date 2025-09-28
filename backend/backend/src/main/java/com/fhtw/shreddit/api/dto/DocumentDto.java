package com.fhtw.shreddit.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pure API DTO used for JSON serialization/deserialization.
 * Decoupled from JPA to avoid any persistence annotations affecting Jackson binding.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDto {
    private Long id;
    private String title;
    private String content;

    public DocumentDto() {}

    @JsonCreator
    public DocumentDto(@JsonProperty("id") Long id,
                       @JsonProperty("title") String title,
                       @JsonProperty("content") String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

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
