package com.orderreturn.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceGenerationJob {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerationJob.class);
    private static final int MAX_RETRIES = 3;

    @Async
    public void generateInvoiceAsync(UUID orderId) {
        int attempt = 0;
        boolean success = false;
        Exception lastException = null;
        while (attempt < MAX_RETRIES && !success) {
            attempt++;
            try {
                simulateInvoiceGeneration(orderId);
                simulateEmailSending(orderId);
                logger.info("Invoice generation and email sending succeeded for order {} on attempt {}", orderId, attempt);
                success = true;
            } catch (Exception ex) {
                lastException = ex;
                logger.warn("Invoice generation failed for order {} on attempt {}: {}", orderId, attempt, ex.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        if (!success) {
            logger.error("Invoice generation job FAILED for order {} after {} attempts. Last error: {}", orderId, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown");
        }
    }

    // Change to package-private for testability
    void simulateInvoiceGeneration(UUID orderId) {
        // Simulate PDF generation (could randomly throw exception for testing retry)
        if (Math.random() < 0.2) throw new RuntimeException("Simulated PDF generation failure");
        logger.info("Simulated PDF invoice generated for order {}", orderId);
    }

    // Change to package-private for testability
    void simulateEmailSending(UUID orderId) {
        // Simulate email sending (could randomly throw exception for testing retry)
        if (Math.random() < 0.2) throw new RuntimeException("Simulated email sending failure");
        logger.info("Simulated invoice email sent for order {}", orderId);
    }
}
