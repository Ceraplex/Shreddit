package com.fhtw.shreddit.search;

public record SearchHit(Long id, IndexedDocument document, double score) {
}
