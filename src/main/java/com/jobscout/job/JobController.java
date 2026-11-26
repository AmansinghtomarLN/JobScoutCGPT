package com.jobscout.job;

import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobPostingRepository repository;
    public JobController(JobPostingRepository repository) { this.repository = repository; }

    @GetMapping
    public Page<JobPosting> list(@RequestParam(defaultValue="0") int page,
                                 @RequestParam(defaultValue="20") int size) {
        return repository.findAll(PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100)));
    }

    @PatchMapping("/{id}/applied")
    public JobPosting updateAppliedStatus(@PathVariable Long id,
                                          @RequestParam boolean applied) {
        JobPosting job = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        job.setApplied(applied);
        return repository.save(job);
    }
}
