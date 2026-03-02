package com.orderreturn.service;

import com.orderreturn.dto.JobExecutionResponse;
import com.orderreturn.entities.JobExecution;
import com.orderreturn.enums.JobStatus;
import com.orderreturn.enums.JobType;
import com.orderreturn.repositories.JobExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobExecutionService {
    private final JobExecutionRepository jobExecutionRepository;

    public JobExecutionService(JobExecutionRepository jobExecutionRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
    }

    public Optional<JobExecution> findExistingRunningOrSuccessJob(UUID entityId, JobType jobType) {
        List<JobExecution> jobs = jobExecutionRepository.findByRelatedEntityIdAndJobTypeAndStatusIn(
            entityId, jobType, Arrays.asList(JobStatus.RUNNING, JobStatus.SUCCESS)
        );
        return jobs.stream().findFirst();
    }

    @Transactional
    public JobExecution createJob(UUID entityId, JobType jobType) {
        Optional<JobExecution> existing = findExistingRunningOrSuccessJob(entityId, jobType);
        if (existing.isPresent()) {
            return existing.get();
        }
        JobExecution job = new JobExecution();
        job.setRelatedEntityId(entityId);
        job.setJobType(jobType);
        job.setStatus(JobStatus.PENDING);
        job.setAttempts(0);
        return jobExecutionRepository.save(job);
    }

    @Transactional
    public void updateJobStatus(UUID jobId, JobStatus status, String error, int attempts) {
        JobExecution job = jobExecutionRepository.findById(jobId).orElseThrow();
        job.setStatus(status);
        job.setLastError(error);
        job.setAttempts(attempts);
        jobExecutionRepository.save(job);
    }

    public Optional<JobExecution> getJobById(UUID jobId) {
        return jobExecutionRepository.findById(jobId);
    }

    public List<JobExecution> getJobsByEntity(UUID entityId) {
        return jobExecutionRepository.findByRelatedEntityId(entityId);
    }

    public List<JobExecution> getJobsByTypeAndEntity(JobType jobType, UUID entityId) {
        return jobExecutionRepository.findByJobTypeAndRelatedEntityId(jobType, entityId);
    }

    public List<JobExecution> getJobsByStatus(JobStatus status) {
        return jobExecutionRepository.findByStatus(status);
    }

    public Page<JobExecution> getJobsPageByEntity(UUID entityId, Pageable pageable) {
        return jobExecutionRepository.findByRelatedEntityId(entityId, pageable);
    }

    public JobExecutionResponse mapToDto(JobExecution job) {
        JobExecutionResponse dto = new JobExecutionResponse();
        dto.setId(job.getId());
        dto.setJobType(job.getJobType());
        dto.setRelatedEntityId(job.getRelatedEntityId());
        dto.setStatus(job.getStatus());
        dto.setAttempts(job.getAttempts());
        dto.setLastError(job.getLastError());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }
}
