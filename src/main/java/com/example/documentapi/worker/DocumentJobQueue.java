package com.example.documentapi.worker;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DocumentJobQueue {

    // Thread-safe queue to hold document IDs for processing
    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

    /**
     * Enqueue a document ID for processing.
     * @param documentId the document ID to enqueue
     */
    public void enqueue(UUID documentId) {
        queue.offer(documentId);
    }

    /**
     * Take a document ID from the queue, blocking if necessary until one is available.
     * @return the document ID
     * @throws InterruptedException if interrupted while waiting
     */
    public UUID take() throws InterruptedException {
        return queue.take();
    }
}