package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.DocumentsApiController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.NativeWebRequest;
import com.fhtw.shreddit.model.Document;

@RestController
public class CustomDocumentsApiController extends DocumentsApiController {

    @Autowired
    public CustomDocumentsApiController(NativeWebRequest request) {
        super(request);
    }

    @Override
    public ResponseEntity<Document> createDocument(Document document) {
        // TODO: Add your logic to save the document and return a response
        // Example:
        // documentRepository.save(document);
        // return ResponseEntity.status(201).body(document);

        // For now, just echo the document back with 201 Created
        return ResponseEntity.status(201).body(document);
    }
}
