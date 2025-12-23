package com.example.documentapi.service;

import com.example.documentapi.model.Document;
import com.example.documentapi.model.DocumentStatus;
import com.example.documentapi.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentStatusUpdater {

    private final DocumentRepository repository;

    /**
     * Mark the document as PROCESSING when the queue starts processing it.
     * @param documentId: the document id.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(UUID documentId) {
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found: " + documentId));
        doc.setStatus(DocumentStatus.PROCESSING);
        doc.setProcessingStartedAt(OffsetDateTime.now());
        doc.setErrorMessage(null);
        repository.saveAndFlush(doc);
    }
}