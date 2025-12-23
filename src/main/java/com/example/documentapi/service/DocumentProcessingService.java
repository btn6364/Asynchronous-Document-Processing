package com.example.documentapi.service;

import com.example.documentapi.model.Document;
import com.example.documentapi.model.DocumentStatus;
import com.example.documentapi.repository.DocumentRepository;
import com.example.documentapi.utils.PDFExtraction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final int MAX_RETRIES = 3;
    private final DocumentRepository repository;
    private final DocumentStatusUpdater statusUpdater;
    private final PDFExtraction pdfExtraction;

    /**
     * Process the documents from the queue
     * Retry the job 3 times if it failed.
     * @param documentId: the document id.
     */
    public void process(UUID documentId) {
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found: " + documentId));

        if (doc.getStatus() == DocumentStatus.DONE
                || doc.getStatus() == DocumentStatus.FAILED
                || doc.getStatus() == DocumentStatus.PROCESSING) {
            return;
        }

        // Get the number of retries so far
        int retryCount = doc.getRetryCount() == null ? 0 : doc.getRetryCount();

        while (retryCount < MAX_RETRIES) {
            // Commit PROCESSING immediately in its own transaction
            statusUpdater.markProcessing(documentId);

            // Reload entity from DB to pick up processingStartedAt set in the REQUIRES_NEW tx
            doc = repository.findById(documentId)
                    .orElseThrow(() -> new IllegalStateException("Document not found: " + documentId));

            try {
                // Long-running work outside any transaction
                Thread.sleep(30000);

                // Simulate PDF real extraction
                String extractedText = pdfExtraction.extractText(doc.getFilename());

                // If successful, mark as DONE and break out of the retry loop
                doc.setExtractedText(extractedText);
                doc.setStatus(DocumentStatus.DONE);
                doc.setCompletedAt(OffsetDateTime.now());
                repository.saveAndFlush(doc);
                break;

            } catch (Exception ex) {
                retryCount += 1;
                doc.setRetryCount(retryCount);
                // If max retries reached, mark as FAILED
                if (retryCount == MAX_RETRIES) {
                    doc.setStatus(DocumentStatus.FAILED);

                    doc.setErrorMessage(ex.getMessage());
                    doc.setCompletedAt(OffsetDateTime.now());
                }
                repository.saveAndFlush(doc);
            }
        }
    }
}
