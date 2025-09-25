package com.fhtw.shreddit.repository;

import com.fhtw.shreddit.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
