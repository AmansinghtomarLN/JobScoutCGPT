package com.jobscout.job;

import java.time.Instant;
import java.time.LocalDate;

public record JobPostingResponse(
        Long id,
        String companyName,
        String externalJobId,
        String title,
        String location,
        String description,
        String jobUrl,
        LocalDate postedDate,
        Instant firstSeenAt,
        boolean applied
) {
    public static JobPostingResponse from(JobPosting job) {
        return new JobPostingResponse(
                job.getId(),
                job.getCompanyName(),
                job.getExternalJobId(),
                job.getTitle(),
                job.getLocation(),
                job.getDescription(),
                job.getJobUrl(),
                job.getPostedDate(),
                job.getFirstSeenAt(),
                job.isApplied()
        );
    }
}
