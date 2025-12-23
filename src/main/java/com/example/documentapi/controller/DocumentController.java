package com.example.documentapi.controller;

import com.example.documentapi.model.Document;
import com.example.documentapi.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    /**
     * Upload a PDF document for asynchronous processing.
     * Accepts a single PDF file and returns the created document id with HTTP 202 (Accepted).
     * The file is validated by content type and extension before delegating to the service.
     *
     * Responses:
     * - 202: Accepted. Body contains the UUID of the created document (string).
     * - 400: Bad Request. File missing, empty or not a PDF.
     *
     * @param file multipart file to upload (must be a PDF)
     * @return ResponseEntity with HTTP 202 and the document UUID on success, or 400 with an error message
     */
    @Operation(summary = "Upload a document (PDF)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Accepted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", format = "uuid", example = "2ff0845c-7ffe-46cd-bbb3-ed973456e9d0")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Only PDF files are accepted")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> upload(
            @Parameter(description = "PDF file to upload", required = true, content = @Content(mediaType = "application/pdf",
                    schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        // Sanity check for PDF file
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is missing or empty");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean pdfByContentType = "application/pdf".equalsIgnoreCase(contentType)
                || "application/x-pdf".equalsIgnoreCase(contentType);
        boolean pdfByExtension = filename != null && filename.toLowerCase().endsWith(".pdf");

        if (!pdfByContentType && !pdfByExtension) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are accepted");
        }


        Document doc = service.upload(file);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(doc.getId());
    }

    /**
     * Retrieve document metadata and extraction result by id.
     * Returns the stored Document object when found (HTTP 200).
     * If the document does not exist, returns HTTP 404 with a plain text error message.
     *
     * Responses:
     * - 200: OK. Body is a JSON representation of the Document.
     * - 404: Not Found. Body contains an error message describing the missing id.
     *
     * @param id UUID of the requested document
     * @return ResponseEntity with HTTP 200 and the Document, or 404 with an error message if not found
     */
    @Operation(summary = "Get document metadata and extraction result")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Document found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Document.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Document with id 8b3f6d1a-9e2c-4baf-8123-5a7d9c0e4f21 not found")
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @Parameter(description = "Document id", required = true) @PathVariable UUID id) {
        Optional<Document> doc = service.getById(id);
        if (doc.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Document with id " + id + " not found");
        }
        return ResponseEntity.ok(doc.get());
    }
}
