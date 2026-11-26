package com.jobscout.job;

import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

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
}
