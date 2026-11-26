package com.jobscout.jobsearch.vodafone;

import com.jobscout.company.Company;
import com.jobscout.company.CompanyRepository;
import com.jobscout.company.InteractionType;
import com.jobscout.job.JobPosting;
import com.jobscout.job.JobPostingRepository;
import com.jobscout.jobsearch.model.DiscoveredJob;
import com.jobscout.jobsearch.model.JobSyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VodafoneJobPersistenceService {

    private static final String COMPANY_KEY = "VODAFONE";
    private static final String COMPANY_NAME = "Vodafone";
    private static final String CAREER_URL = "https://jobs.vodafone.com/careers";

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final VodafoneJobSearchService searchService;

    public VodafoneJobPersistenceService(CompanyRepository companyRepository,
                                         JobPostingRepository jobPostingRepository,
                                         VodafoneJobSearchService searchService) {
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.searchService = searchService;
    }

    @Transactional
    public JobSyncResult searchAndSave(String query, String location, int start) {
        List<DiscoveredJob> discoveredJobs = searchService.search(query, location, start);

        Company company = companyRepository.findByCompanyKey(COMPANY_KEY)
                .orElseGet(() -> companyRepository.save(new Company(
                        COMPANY_KEY, COMPANY_NAME, CAREER_URL, InteractionType.API, true)));

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (DiscoveredJob discovered : discoveredJobs) {
            if (discovered.externalJobId() == null || discovered.externalJobId().isBlank()
                    || discovered.title() == null || discovered.title().isBlank()) {
                skipped++;
                continue;
            }

            JobPosting existing = jobPostingRepository
                    .findByCompanyAndExternalJobId(company, discovered.externalJobId())
                    .orElse(null);

            if (existing == null) {
                jobPostingRepository.save(new JobPosting(
                        company,
                        discovered.externalJobId(),
                        discovered.title(),
                        discovered.location(),
                        discovered.description(),
                        discovered.jobUrl(),
                        discovered.postedDate()));
                inserted++;
            } else {
                existing.updateDetails(
                        discovered.title(),
                        discovered.location(),
                        discovered.description(),
                        discovered.jobUrl(),
                        discovered.postedDate());
                updated++;
            }
        }

        return new JobSyncResult(COMPANY_NAME, discoveredJobs.size(), inserted, updated, skipped);
    }
}
