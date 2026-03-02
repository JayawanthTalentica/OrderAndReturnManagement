package com.orderreturn.service;

import com.orderreturn.entities.JobExecution;
import com.orderreturn.enums.JobStatus;
import com.orderreturn.enums.JobType;
import com.orderreturn.repositories.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobExecutionServiceTest {
    private JobExecutionRepository jobExecutionRepository;
    private JobExecutionService jobExecutionService;

    @BeforeEach
    void setUp() {
        jobExecutionRepository = mock(JobExecutionRepository.class);
        jobExecutionService = new JobExecutionService(jobExecutionRepository);
    }

    @Test
    void testIdempotentJobCreation() {
        UUID entityId = UUID.randomUUID();
        JobExecution existingJob = new JobExecution();
        existingJob.setId(UUID.randomUUID());
        existingJob.setJobType(JobType.INVOICE_GENERATION);
        existingJob.setRelatedEntityId(entityId);
        existingJob.setStatus(JobStatus.SUCCESS);
        when(jobExecutionRepository.findByRelatedEntityIdAndJobTypeAndStatusIn(eq(entityId), eq(JobType.INVOICE_GENERATION), anyList()))
                .thenReturn(Collections.singletonList(existingJob));
        JobExecution job = jobExecutionService.createJob(entityId, JobType.INVOICE_GENERATION);
        assertEquals(existingJob, job);
        verify(jobExecutionRepository, never()).save(any(JobExecution.class));
    }

    @Test
    void testRetryIncrementAndFinalStates() {
        UUID entityId = UUID.randomUUID();
        JobExecution job = new JobExecution();
        job.setId(UUID.randomUUID());
        job.setJobType(JobType.REFUND_PROCESSING);
        job.setRelatedEntityId(entityId);
        job.setStatus(JobStatus.PENDING);
        job.setAttempts(0);
        when(jobExecutionRepository.findById(job.getId())).thenReturn(Optional.of(job));
        jobExecutionService.updateJobStatus(job.getId(), JobStatus.RUNNING, null, 1);
        assertEquals(JobStatus.RUNNING, job.getStatus());
        assertEquals(1, job.getAttempts());
        jobExecutionService.updateJobStatus(job.getId(), JobStatus.FAILED, "error", 3);
        assertEquals(JobStatus.FAILED, job.getStatus());
        assertEquals(3, job.getAttempts());
        assertEquals("error", job.getLastError());
        jobExecutionService.updateJobStatus(job.getId(), JobStatus.SUCCESS, null, 2);
        assertEquals(JobStatus.SUCCESS, job.getStatus());
        assertEquals(2, job.getAttempts());
    }
}
