package com.fhtw.shreddit.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new comment.
 */
public class CreateCommentRequest {
    @NotBlank(message = "Comment text must not be blank")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}