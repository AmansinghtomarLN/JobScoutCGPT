package com.jobscout.jobsearch;

import com.jobscout.jobsearch.model.DiscoveredJob;
import com.jobscout.jobsearch.model.JobSyncResult;
import com.jobscout.jobsearch.service.AutodeskJobPersistenceService;
import com.jobscout.jobsearch.service.AutodeskJobSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-search/autodesk")
public class AutodeskJobSearchController {

    private final AutodeskJobSearchService searchService;
    private final AutodeskJobPersistenceService persistenceService;

    public AutodeskJobSearchController(AutodeskJobSearchService searchService,
                                       AutodeskJobPersistenceService persistenceService) {
        this.searchService = searchService;
        this.persistenceService = persistenceService;
    }

    @GetMapping
    public List<DiscoveredJob> search(
            @RequestParam(defaultValue = "Java") String query,
            @RequestParam(defaultValue = "India") String location,
            @RequestParam(defaultValue = "0") int start) {
        return searchService.search(query, location, start);
    }

    @PostMapping("/sync")
    public JobSyncResult searchAndSave(
            @RequestParam(defaultValue = "Java") String query,
            @RequestParam(defaultValue = "India") String location,
            @RequestParam(defaultValue = "0") int start) {
        return persistenceService.searchAndSave(query, location, start);
    }
}
