package com.orderreturn.service.job;

import com.orderreturn.entities.JobExecution;
import com.orderreturn.repositories.JobExecutionRepository;
import com.orderreturn.service.JobExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceGenerationJobTest {
    InvoiceGenerationJob job;
    JobExecutionRepository jobExecutionRepository;
    JobExecutionService jobExecutionService;

    @BeforeEach
    void setup() {
        jobExecutionRepository = mock(JobExecutionRepository.class);
        jobExecutionService = mock(JobExecutionService.class);
        job = new InvoiceGenerationJob(jobExecutionRepository, jobExecutionService);
        JobExecution mockJob = new JobExecution();
        mockJob.setId(UUID.randomUUID());
        when(jobExecutionService.createJob(any(), any())).thenReturn(mockJob);
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
