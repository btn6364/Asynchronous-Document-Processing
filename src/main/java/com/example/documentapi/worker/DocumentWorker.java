package com.example.documentapi.worker;

import com.example.documentapi.service.DocumentProcessingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class DocumentWorker {

    // Adjust the pool size as needed (e.g., based on CPU)
    private final static int NUMBER_OF_THREADS = 4;
    private final DocumentProcessingService documentProcessingService;
    private final DocumentJobQueue jobQueue;
    private final DocumentProcessingService processingService;
    private final ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private volatile boolean running = true;
    private Thread dispatcher;

    /**
     * Start the dispatcher thread that listens for jobs and submits them to the pool.
     */
    @PostConstruct
    public void start() {
        dispatcher = new Thread(() -> {
            while (running) {
                try {
                    // Blocks until an ID is available
                    UUID documentId = jobQueue.take();

                    // Process concurrently
                    pool.submit(() -> processingService.process(documentId));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "document-worker-dispatcher");

        dispatcher.start();
    }

    /**
     * Stop the dispatcher and shutdown the pool.
     */
    @PreDestroy
    public void stop() {
        running = false;
        if (dispatcher != null) dispatcher.interrupt();
        pool.shutdownNow();
    }
}
