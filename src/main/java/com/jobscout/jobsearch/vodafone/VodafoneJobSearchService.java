package com.jobscout.jobsearch.vodafone;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobscout.jobsearch.model.DiscoveredJob;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class VodafoneJobSearchService {

    private static final String SEARCH_PATH = "/api/pcsx/search";
    private static final String DETAILS_PATH = "/api/pcsx/position_details";
    private static final String DOMAIN = "vodafone.com";
    private static final ZoneId POSTING_DATE_ZONE = ZoneId.of("Asia/Kolkata");

    private final RestClient restClient;
    private final VodafoneJobParser parser;

    public VodafoneJobSearchService(RestClient.Builder restClientBuilder,
                                    VodafoneJobParser parser) {
        this.restClient = restClientBuilder
                .baseUrl("https://jobs.vodafone.com")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Referer", "https://jobs.vodafone.com/careers")
                .build();
        this.parser = parser;
    }

    public List<DiscoveredJob> search(String query, String location, int start) {
        JsonNode searchResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SEARCH_PATH)
                        .queryParam("domain", DOMAIN)
                        .queryParam("query", query)
                        .queryParam("location", location)
                        .queryParam("start", Math.max(0, start))
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (searchResponse == null || searchResponse.path("status").asInt(500) != 200) {
            throw new IllegalStateException("Vodafone Eightfold job search API returned an unsuccessful response");
        }

        List<DiscoveredJob> matchingJobs = new ArrayList<>();
        for (JsonNode position : parser.extractPositions(searchResponse)) {
            String positionId = position.path("id").asText("");
            if (positionId.isBlank()) {
                continue;
            }

            JsonNode detailsResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(DETAILS_PATH)
                            .queryParam("position_id", positionId)
                            .queryParam("domain", DOMAIN)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (detailsResponse != null && detailsResponse.path("status").asInt(500) == 200) {
                DiscoveredJob job = parser.parseJob(detailsResponse);
                LocalDate cutoffDate = LocalDate.now(POSTING_DATE_ZONE).minusMonths(1);
                if (job.postedDate() != null
                        && !job.postedDate().isBefore(cutoffDate)
                        && parser.matchesSearchPhrases(job, query)) {
                    matchingJobs.add(job);
                }
            }
        }
        return matchingJobs;
    }
}
