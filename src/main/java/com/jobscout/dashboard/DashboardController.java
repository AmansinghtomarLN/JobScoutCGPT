package com.jobscout.dashboard;

import com.jobscout.company.CompanyRepository;
import com.jobscout.job.JobPostingRepository;
import com.jobscout.run.ExecutionRunRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CompanyRepository companies;
    private final JobPostingRepository jobs;
    private final ExecutionRunRepository runs;
    public DashboardController(CompanyRepository companies, JobPostingRepository jobs, ExecutionRunRepository runs) {
        this.companies=companies; this.jobs=jobs; this.runs=runs;
    }
    @GetMapping("/summary")
    public Map<String,Long> summary() {
        return Map.of("totalCompanies",companies.count(),
            "enabledCompanies",companies.countByEnabledTrue(),
            "totalJobs",jobs.count(),"totalRuns",runs.count());
    }
}
