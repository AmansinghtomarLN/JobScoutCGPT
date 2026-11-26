package com.jobscout.jobsearch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/job-search")
public class CompanyScanController {

    private final CompanyScanOrchestrator orchestrator;

    public CompanyScanController(CompanyScanOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/scan-all")
    public Map<String, Object> startScanAll() {
        return orchestrator.startScanAll();
    }

    @GetMapping("/scan-all/{scanId}")
    public Map<String, Object> getScanProgress(@PathVariable String scanId) {
        try {
            return orchestrator.getProgress(scanId);
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
