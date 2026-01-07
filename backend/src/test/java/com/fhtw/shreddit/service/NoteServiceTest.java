package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.NoteDto;
import com.fhtw.shreddit.exception.DocumentException;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.model.NoteEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import com.fhtw.shreddit.repository.NoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private DocumentRepository documentRepository;

    private NoteService noteService;

    @BeforeEach
    void setup() {
        noteService = new NoteService(noteRepository, documentRepository);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice", "pw"));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listNotesReturnsNotesForOwner() {
        DocumentEntity doc = new DocumentEntity();
        doc.setId(1L);
        doc.setUsername("alice");
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        NoteEntity entity = new NoteEntity();
        entity.setId(5L);
        entity.setDocumentId(1L);
        entity.setAuthor("alice");
        entity.setContent("hello");
        entity.setCreatedAt(LocalDateTime.now());

        when(noteRepository.findByDocumentIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(entity));

        List<NoteDto> notes = noteService.listNotes(1L);

        assertEquals(1, notes.size());
        assertEquals(entity.getContent(), notes.get(0).getContent());
    }

    @Test
    void addNoteThrowsWhenNotOwner() {
        DocumentEntity doc = new DocumentEntity();
        doc.setId(2L);
        doc.setUsername("bob");
        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(doc));

        assertThrows(DocumentException.class, () -> noteService.addNote(2L, "test"));
    }

    @Test
    void addNoteRejectsEmptyContent() {
        DocumentEntity doc = new DocumentEntity();
        doc.setId(3L);
        doc.setUsername("alice");
        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(doc));

        assertThrows(DocumentException.class, () -> noteService.addNote(3L, " "));
    }
}
