package com.jobscout.job;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobPostingRepository repository;

    public JobController(JobPostingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<JobPostingResponse> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<JobPosting> jobs = repository.findAll(
                PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100)));
        return jobs.map(JobPostingResponse::from);
    }

    @PatchMapping("/{id}/applied")
    @Transactional
    public JobPostingResponse updateAppliedStatus(@PathVariable Long id,
                                                  @RequestParam boolean applied) {
        JobPosting job = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        job.setApplied(applied);
        return JobPostingResponse.from(repository.save(job));
    }
}
