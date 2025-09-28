package com.fhtw.shreddit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_entity", schema = "public")
public class DocumentEntity extends Document {
    // inherits id/title/content mappings from @MappedSuperclass doc
}
