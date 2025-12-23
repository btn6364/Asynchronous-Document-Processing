package com.example.documentapi.service;

import com.example.documentapi.model.*;
import com.example.documentapi.repository.DocumentRepository;
import com.example.documentapi.worker.DocumentJobQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repository;
    private final DocumentJobQueue jobQueue;

    @Value("${app.files.base-dir:/data/files}")
    private String filesBaseDir;

    /**
     * Save the document and enqueue a processing job.
     * @param file the file to process
     * @return the created Document entity
     */
    public Document upload(MultipartFile file) {
        try {
            Path base = Paths.get(filesBaseDir);
            Files.createDirectories(base);
            String originalFileName = Optional.ofNullable(file.getOriginalFilename()).orElse("file.pdf");
            Path dest = base.resolve(originalFileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // Save a new Document entity
            Document doc = new Document();
            doc.setFilename(originalFileName);
            doc.setContentType(file.getContentType());
            doc.setStatus(DocumentStatus.UPLOADED);
            doc.setUploadedAt(OffsetDateTime.now());
            doc.setRetryCount(0);
            doc = repository.save(doc);

            // Enqueue job after saving
            jobQueue.enqueue(doc.getId());

            // Return the created document
            return doc;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save uploaded file", ex);
        }
    }

    /**
     * Return the document based on its id.
     * @param id the id of the document
     * @return the Document entity wrapped in an Optional
     */
    public Optional<Document> getById(UUID id) {
        return repository.findById(id);
    }
}
