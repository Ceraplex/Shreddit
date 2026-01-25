package com.fhtw.xmlimportworker.repo;

import com.fhtw.xmlimportworker.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
