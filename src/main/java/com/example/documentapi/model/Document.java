package com.example.documentapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    private String filename;

    private String contentType;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private Integer retryCount;

    private String errorMessage;

    private OffsetDateTime uploadedAt;
    private OffsetDateTime processingStartedAt;
    private OffsetDateTime completedAt;
}
