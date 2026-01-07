package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.NoteDto;
import com.fhtw.shreddit.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentNotesControllerTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private DocumentNotesController controller;

    private NoteDto note;

    @BeforeEach
    void setup() {
        note = new NoteDto();
        note.setId(1L);
        note.setDocumentId(5L);
        note.setAuthor("alice");
        note.setContent("hello");
        note.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void listNotesReturnsNotes() {
        when(noteService.listNotes(5L)).thenReturn(List.of(note));

        ResponseEntity<List<NoteDto>> response = controller.listNotes(5L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void addNoteCreatesNote() {
        when(noteService.addNote(5L, "hello")).thenReturn(note);

        NoteDto input = new NoteDto();
        input.setContent("hello");
        ResponseEntity<NoteDto> response = controller.addNote(5L, input);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(note.getId(), response.getBody().getId());
    }
}
