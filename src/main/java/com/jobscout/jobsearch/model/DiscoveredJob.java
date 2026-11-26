package com.jobscout.jobsearch.model;

import java.time.LocalDate;

public record DiscoveredJob(
        String company,
        String externalJobId,
        String title,
        String location,
        String description,
        String jobUrl,
        LocalDate postedDate,
        String workLocationOption
) {
}
