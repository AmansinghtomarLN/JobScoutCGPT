package com.jobscout.jobsearch;

import com.jobscout.jobsearch.model.DiscoveredJob;
import com.jobscout.jobsearch.service.AutodeskJobSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-search/autodesk")
public class AutodeskJobSearchController {

    private final AutodeskJobSearchService searchService;

    public AutodeskJobSearchController(AutodeskJobSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<DiscoveredJob> search(
            @RequestParam(defaultValue = "Java") String query,
            @RequestParam(defaultValue = "India") String location,
            @RequestParam(defaultValue = "0") int start) {
        return searchService.search(query, location, start);
    }
}
