package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.NoteDto;
import com.fhtw.shreddit.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DocumentNotesController {
    private static final Logger log = LoggerFactory.getLogger(DocumentNotesController.class);

    private final NoteService noteService;

    public DocumentNotesController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/documents/{id}/notes")
    public ResponseEntity<List<NoteDto>> listNotes(@PathVariable("id") Long documentId) {
        return ResponseEntity.ok(noteService.listNotes(documentId));
    }

    @GetMapping("/api/documents/{id}/notes")
    public ResponseEntity<List<NoteDto>> listNotesApi(@PathVariable("id") Long documentId) {
        return ResponseEntity.ok(noteService.listNotes(documentId));
    }

    @PostMapping("/documents/{id}/notes")
    public ResponseEntity<NoteDto> addNote(@PathVariable("id") Long documentId, @RequestBody NoteDto noteDto) {
        log.debug("Adding note for doc {} with body {}", documentId, noteDto);
        String content = noteDto != null ? noteDto.getContent() : null;
        NoteDto created = noteService.addNote(documentId, content);
        return ResponseEntity.status(201).body(created);
    }

    @PostMapping("/api/documents/{id}/notes")
    public ResponseEntity<NoteDto> addNoteApi(@PathVariable("id") Long documentId, @RequestBody NoteDto noteDto) {
        String content = noteDto != null ? noteDto.getContent() : null;
        NoteDto created = noteService.addNote(documentId, content);
        return ResponseEntity.status(201).body(created);
    }
}
