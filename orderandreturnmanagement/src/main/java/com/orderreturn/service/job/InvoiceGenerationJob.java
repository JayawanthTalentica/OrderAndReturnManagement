package com.orderreturn.service.job;

import com.orderreturn.entities.JobExecution;
import com.orderreturn.enums.JobStatus;
import com.orderreturn.enums.JobType;
import com.orderreturn.repositories.JobExecutionRepository;
import com.orderreturn.service.JobExecutionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class InvoiceGenerationJob {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerationJob.class);
    private static final int MAX_RETRIES = 3;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionService jobExecutionService;

    @Autowired
    public InvoiceGenerationJob(JobExecutionRepository jobExecutionRepository, JobExecutionService jobExecutionService) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobExecutionService = jobExecutionService;
    }

    @Async
    public void generateInvoiceAsync(UUID orderId) {
        // Idempotency check and job creation
        JobExecution job = jobExecutionService.createJob(orderId, JobType.INVOICE_GENERATION);
        int attempt = 0;
        boolean success = false;
        Exception lastException = null;
        jobExecutionService.updateJobStatus(job.getId(), JobStatus.RUNNING, null, attempt);
        while (attempt < MAX_RETRIES && !success) {
            attempt++;
            jobExecutionService.updateJobStatus(job.getId(), JobStatus.RUNNING, null, attempt);
            try {
                simulateInvoiceGeneration(orderId);
                simulateEmailSending(orderId);
                logger.info("Invoice generation and email sending succeeded for order {} on attempt {}", orderId, attempt);
                success = true;
                jobExecutionService.updateJobStatus(job.getId(), JobStatus.SUCCESS, null, attempt);
            } catch (Exception ex) {
                lastException = ex;
                jobExecutionService.updateJobStatus(job.getId(), JobStatus.RUNNING, ex.getMessage(), attempt);
                logger.warn("Invoice generation failed for order {} on attempt {}: {}", orderId, attempt, ex.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        if (!success) {
            jobExecutionService.updateJobStatus(job.getId(), JobStatus.FAILED, lastException != null ? lastException.getMessage() : "unknown", attempt);
            logger.error("Invoice generation job FAILED for order {} after {} attempts. Last error: {}", orderId, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown");
        }
    }

    // Change to package-private for testability
    void simulateInvoiceGeneration(UUID orderId) {
        // Simulate PDF generation (could randomly throw exception for testing retry)
        if (Math.random() < 0.2) throw new RuntimeException("Simulated PDF generation failure");
        // Generate dummy PDF and store in 'invoices' folder at project root
        String folderPath = System.getProperty("user.dir") + File.separator + "invoices";
        String fileName = String.format("invoice-%s.pdf", orderId);
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Failed to create invoices directory at: " + folderPath);
        }
        File pdfFile = new File(folder, fileName);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(100, 750);
                contentStream.showText("Invoice for Order: " + orderId);
                contentStream.endText();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 720);
                contentStream.showText("This is a dummy invoice generated on: " + java.time.LocalDateTime.now());
                contentStream.endText();
            }
            document.save(pdfFile);
            logger.info("Dummy PDF invoice generated and saved at {} for order {}", pdfFile.getAbsolutePath(), orderId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF invoice: " + e.getMessage(), e);
        }
    }

    // Change to package-private for testability
    void simulateEmailSending(UUID orderId) {
        // Simulate email sending (could randomly throw exception for testing retry)
        if (Math.random() < 0.2) throw new RuntimeException("Simulated email sending failure");
        logger.info("Simulated invoice email sent for order {}", orderId);
    }
}
