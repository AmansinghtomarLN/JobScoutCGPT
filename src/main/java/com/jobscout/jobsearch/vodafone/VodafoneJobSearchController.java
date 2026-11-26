package com.jobscout.jobsearch.vodafone;

import com.jobscout.jobsearch.model.DiscoveredJob;
import com.jobscout.jobsearch.model.JobSyncResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-search/vodafone")
public class VodafoneJobSearchController {

    private final VodafoneJobSearchService searchService;
    private final VodafoneJobPersistenceService persistenceService;

    public VodafoneJobSearchController(VodafoneJobSearchService searchService,
                                       VodafoneJobPersistenceService persistenceService) {
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
