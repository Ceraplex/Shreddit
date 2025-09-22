package com.fhtw.shreddit.repository;

import com.fhtw.shreddit.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
