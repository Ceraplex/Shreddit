package com.fhtw.genaiworker.repo;

import com.fhtw.genaiworker.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
