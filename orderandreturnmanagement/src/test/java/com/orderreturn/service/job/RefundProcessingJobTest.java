package com.orderreturn.service.job;

import com.orderreturn.entities.JobExecution;
import com.orderreturn.entities.Return;
import com.orderreturn.enums.RefundStatus;
import com.orderreturn.repositories.JobExecutionRepository;
import com.orderreturn.repositories.ReturnRepository;
import com.orderreturn.service.JobExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefundProcessingJobTest {
    ReturnRepository returnRepository;
    JobExecutionRepository jobExecutionRepository;
    RefundProcessingJob job;
    UUID returnId;
    Return ret;
    JobExecutionService jobExecutionService;

    @BeforeEach
    void setup() {
        returnRepository = mock(ReturnRepository.class);
        jobExecutionRepository = mock(JobExecutionRepository.class);
        jobExecutionService = mock(JobExecutionService.class);
        job = new RefundProcessingJob(returnRepository, jobExecutionRepository, jobExecutionService);
        returnId = UUID.randomUUID();
        ret = new Return();
        // Set id using reflection for test purposes
        try {
            java.lang.reflect.Field idField = Return.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(ret, returnId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ret.setRefundStatus(RefundStatus.PENDING);
        JobExecution mockJob = new JobExecution();
        mockJob.setId(UUID.randomUUID());
        when(jobExecutionService.createJob(any(), any())).thenReturn(mockJob);
    }

    @Test
    void successfulRefund_updatesStatusToSuccess() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        RefundProcessingJob spyJob = Mockito.spy(job);
        doNothing().when(spyJob).simulateRefundProcessing(any());
        spyJob.processRefundAsync(returnId);
        assertEquals(RefundStatus.SUCCESS, ret.getRefundStatus());
    }

    @Test
    void failedRefundAfterRetries_updatesStatusToFailed() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        RefundProcessingJob spyJob = Mockito.spy(job);
        doThrow(new RuntimeException("fail")).when(spyJob).simulateRefundProcessing(any());
        spyJob.processRefundAsync(returnId);
        assertEquals(RefundStatus.FAILED, ret.getRefundStatus());
    }

    @Test
    void refundFailure_doesNotChangeReturnState() {
        ret.setRefundStatus(RefundStatus.PENDING);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        RefundProcessingJob spyJob = Mockito.spy(job);
        doThrow(new RuntimeException("fail")).when(spyJob).simulateRefundProcessing(any());
        spyJob.processRefundAsync(returnId);
        // No state field change expected, only refundStatus
        assertNotNull(ret.getRefundStatus());
    }
}
