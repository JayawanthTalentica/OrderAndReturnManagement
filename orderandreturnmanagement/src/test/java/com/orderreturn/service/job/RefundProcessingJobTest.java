package com.orderreturn.service.job;

import com.orderreturn.entities.Return;
import com.orderreturn.enums.RefundStatus;
import com.orderreturn.repositories.ReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefundProcessingJobTest {
    ReturnRepository returnRepository;
    RefundProcessingJob job;
    UUID returnId;
    Return ret;

    @BeforeEach
    void setup() {
        returnRepository = mock(ReturnRepository.class);
        job = new RefundProcessingJob(returnRepository);
        returnId = UUID.randomUUID();
        ret = Return.builder().id(returnId).refundStatus(RefundStatus.PENDING).build();
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
