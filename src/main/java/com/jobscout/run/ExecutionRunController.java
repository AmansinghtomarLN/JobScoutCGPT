package com.jobscout.run;

import com.jobscout.company.CompanyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/runs")
public class ExecutionRunController {
    private final ExecutionRunRepository runs;
    private final CompanyRepository companies;
    public ExecutionRunController(ExecutionRunRepository runs, CompanyRepository companies) {
        this.runs=runs; this.companies=companies;
    }
    @GetMapping public List<ExecutionRun> list(){ return runs.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ExecutionRun create(@Valid @RequestBody CreateRunRequest request) {
        if (companies.findByCompanyKey(request.companyKey()).isEmpty()) {
            throw new IllegalArgumentException("Unknown company key: " + request.companyKey());
        }
        // This starter records the requested run. Actual crawl orchestration is a future milestone.
        return runs.save(new ExecutionRun(request.companyKey(), request.keyword(),
                request.location(), request.postedWithinDays()));
    }
}
