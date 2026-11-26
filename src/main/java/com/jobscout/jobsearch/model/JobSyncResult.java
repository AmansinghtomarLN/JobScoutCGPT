package com.jobscout.jobsearch.model;

public record JobSyncResult(
        String company,
        int discovered,
        int inserted,
        int updated,
        int skipped
) {
}
