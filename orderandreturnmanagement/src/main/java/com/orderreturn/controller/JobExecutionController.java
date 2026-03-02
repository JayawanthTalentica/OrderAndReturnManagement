package com.orderreturn.controller;

import com.orderreturn.dto.JobExecutionResponse;
import com.orderreturn.dto.PageResponse;
import com.orderreturn.entities.JobExecution;
import com.orderreturn.service.JobExecutionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.orderreturn.exception.NotFoundException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/jobs")
public class JobExecutionController {
    private final JobExecutionService jobExecutionService;

    public JobExecutionController(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    private PageResponse<JobExecutionResponse> getPaginatedJobs(UUID entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobExecution> jobPage = jobExecutionService.getJobsPageByEntity(entityId, pageable);
        List<JobExecutionResponse> content = jobPage.getContent().stream()
            .map(jobExecutionService::mapToDto)
            .collect(Collectors.toList());
        return new PageResponse<>(content, jobPage.getTotalElements(), jobPage.getTotalPages());
    }

    @GetMapping("/{jobId}")
    public JobExecutionResponse getJobById(@PathVariable UUID jobId) {
        JobExecution job = jobExecutionService.getJobById(jobId)
            .orElseThrow(() -> new NotFoundException("JobExecution not found for id: " + jobId));
        return jobExecutionService.mapToDto(job);
    }

    @GetMapping
    public PageResponse<JobExecutionResponse> getJobsByEntity(
            @RequestParam UUID entityId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getPaginatedJobs(entityId, page, size);
    }

    @GetMapping("/orders/{orderId}/jobs")
    public PageResponse<JobExecutionResponse> getJobsForOrder(
            @PathVariable UUID orderId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getPaginatedJobs(orderId, page, size);
    }

    @GetMapping("/returns/{returnId}/jobs")
    public PageResponse<JobExecutionResponse> getJobsForReturn(
            @PathVariable UUID returnId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getPaginatedJobs(returnId, page, size);
    }
}
