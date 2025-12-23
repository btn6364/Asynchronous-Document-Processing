package com.example.documentapi.service;

import com.example.documentapi.model.Document;
import com.example.documentapi.model.DocumentStatus;
import com.example.documentapi.repository.DocumentRepository;
import com.example.documentapi.worker.DocumentJobQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private DocumentJobQueue jobQueue;

    private DocumentService service;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        service = new DocumentService(repository, jobQueue);
        tempDir = Files.createTempDirectory("document-service-test");
        ReflectionTestUtils.setField(service, "filesBaseDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void uploadSavesFileAndEnqueuesJobAndReturnsDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mydoc.pdf",
                "application/pdf",
                "hello-pdf".getBytes()
        );

        // Make repository.save return the same document with an id set
        when(repository.save(any(Document.class))).thenAnswer(invocation -> {
            Document d = invocation.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        Document result = service.upload(file);

        assertNotNull(result.getId());
        assertEquals("mydoc.pdf", result.getFilename());
        assertEquals(DocumentStatus.UPLOADED, result.getStatus());
        assertEquals(0, result.getRetryCount());

        // verify file exists on disk
        Path stored = tempDir.resolve("mydoc.pdf");
        assertTrue(Files.exists(stored));
        byte[] content = Files.readAllBytes(stored);
        assertArrayEquals("hello-pdf".getBytes(), content);

        // verify repository.save called and jobQueue.enqueue called with id
        verify(repository, times(1)).save(any(Document.class));
        verify(jobQueue, times(1)).enqueue(result.getId());
    }

    @Test
    void getByIdDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(doc));

        Optional<Document> res = service.getById(id);
        assertTrue(res.isPresent());
        assertEquals(id, res.get().getId());
        verify(repository, times(1)).findById(id);
    }
}
