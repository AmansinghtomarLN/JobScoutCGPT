package com.jobscout.jobsearch;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/job-search")
public class CompanyScanController {

    private final CompanyScanOrchestrator orchestrator;

    public CompanyScanController(CompanyScanOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/scan-all")
    public Map<String, Object> scanAll() {
        return orchestrator.scanAll();
    }
}
