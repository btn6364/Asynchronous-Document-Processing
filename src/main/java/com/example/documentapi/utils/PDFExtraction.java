package com.example.documentapi.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public final class PDFExtraction {

    // Base directory for files, injected from application properties
    @Value("${app.files.base-dir:/data/files}")
    private String filesBaseDir;

    private PDFExtraction() {}

    /**
     * Extract text from a PDF file at the given path.
     * @param path the path to the PDF file
     * @return the extracted text
     * @throws IOException if an I/O error occurs
     */
    public String extractText(Path path) throws IOException {
        try (PDDocument doc = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /**
     * Extract text from a PDF file at the given file name.
     * @param filename the name of the PDF file
     * @return the extracted text
     * @throws IOException if an I/O error occurs
     */
    public String extractText(String filename) throws IOException {
        Path pdfPath = Paths.get(filesBaseDir, filename);
        return extractText(pdfPath);
    }
}