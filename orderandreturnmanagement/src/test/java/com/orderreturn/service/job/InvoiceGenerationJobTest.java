package com.orderreturn.service.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceGenerationJobTest {
    InvoiceGenerationJob job;

    @BeforeEach
    void setup() {
        job = new InvoiceGenerationJob();
    }

    @Test
    void successfulExecution_completesWithoutException() {
        assertDoesNotThrow(() -> job.generateInvoiceAsync(UUID.randomUUID()));
    }

    @Test
    void retryLogic_invokedOnFailure() {
        InvoiceGenerationJob spyJob = Mockito.spy(job);
        doThrow(new RuntimeException("fail1")).doThrow(new RuntimeException("fail2")).doNothing().when(spyJob).simulateInvoiceGeneration(any());
        doNothing().when(spyJob).simulateEmailSending(any());
        assertDoesNotThrow(() -> spyJob.generateInvoiceAsync(UUID.randomUUID()));
        verify(spyJob, times(3)).simulateInvoiceGeneration(any());
    }

    @Test
    void failureAfter3Retries_doesNotThrow() {
        InvoiceGenerationJob spyJob = Mockito.spy(job);
        doThrow(new RuntimeException("fail")).when(spyJob).simulateInvoiceGeneration(any());
        doNothing().when(spyJob).simulateEmailSending(any());
        assertDoesNotThrow(() -> spyJob.generateInvoiceAsync(UUID.randomUUID()));
        verify(spyJob, times(3)).simulateInvoiceGeneration(any());
    }
}

