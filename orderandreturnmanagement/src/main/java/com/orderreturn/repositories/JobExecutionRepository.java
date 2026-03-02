package com.orderreturn.repositories;

import com.orderreturn.entities.JobExecution;
import com.orderreturn.enums.JobStatus;
import com.orderreturn.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    List<JobExecution> findByRelatedEntityId(UUID relatedEntityId);
    List<JobExecution> findByRelatedEntityIdAndJobTypeAndStatusIn(UUID relatedEntityId, JobType jobType, List<JobStatus> statuses);
    List<JobExecution> findByJobTypeAndRelatedEntityId(JobType jobType, UUID relatedEntityId);
    List<JobExecution> findByStatus(JobStatus status);
    Page<JobExecution> findByRelatedEntityId(UUID relatedEntityId, Pageable pageable);
}
