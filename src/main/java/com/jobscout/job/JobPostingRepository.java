package com.jobscout.job;

import com.jobscout.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Optional<JobPosting> findByCompanyAndExternalJobId(Company company, String externalJobId);
}
