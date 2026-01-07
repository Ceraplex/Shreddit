package com.fhtw.indexingworker.repo;

import com.fhtw.indexingworker.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
