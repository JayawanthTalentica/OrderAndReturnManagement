package com.orderreturn.service.job;

import com.orderreturn.entities.Return;
import com.orderreturn.enums.RefundStatus;
import com.orderreturn.repositories.ReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefundProcessingJob {
    private static final Logger logger = LoggerFactory.getLogger(RefundProcessingJob.class);
    private static final int MAX_RETRIES = 3;
    private final ReturnRepository returnRepository;

    public RefundProcessingJob(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Async
    public void processRefundAsync(UUID returnId) {
        int attempt = 0;
        boolean success = false;
        Exception lastException = null;
        while (attempt < MAX_RETRIES && !success) {
            attempt++;
            try {
                simulateRefundProcessing(returnId);
                updateRefundStatus(returnId, RefundStatus.SUCCESS);
                logger.info("Refund processed successfully for return {} on attempt {}", returnId, attempt);
                success = true;
            } catch (Exception ex) {
                lastException = ex;
                logger.warn("Refund processing failed for return {} on attempt {}: {}", returnId, attempt, ex.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        if (!success) {
            updateRefundStatus(returnId, RefundStatus.FAILED);
            logger.error("Refund processing job FAILED for return {} after {} attempts. Last error: {}", returnId, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown");
        }
    }

    // Change to package-private for testability
    void simulateRefundProcessing(UUID returnId) {
        // Simulate external payment gateway call (randomly fail for retry logic)
        if (Math.random() < 0.2) throw new RuntimeException("Simulated payment gateway failure");
        logger.info("Simulated refund processed for return {}", returnId);
    }

    @Transactional
    protected void updateRefundStatus(UUID returnId, RefundStatus status) {
        Optional<Return> retOpt = returnRepository.findById(returnId);
        if (retOpt.isPresent()) {
            Return ret = retOpt.get();
            ret.setRefundStatus(status);
            returnRepository.save(ret);
        }
    }
}

