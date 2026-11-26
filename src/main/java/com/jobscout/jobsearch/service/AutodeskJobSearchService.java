package com.jobscout.jobsearch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobscout.jobsearch.model.DiscoveredJob;
import com.jobscout.jobsearch.parser.autodesk.AutodeskJobParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutodeskJobSearchService {

    private static final String SEARCH_PATH = "/api/pcsx/search";
    private static final String DETAILS_PATH = "/api/pcsx/position_details";
    private static final String DOMAIN = "autodesk.com";

    private final RestClient restClient;
    private final AutodeskJobParser parser;

    public AutodeskJobSearchService(RestClient.Builder restClientBuilder, AutodeskJobParser parser) {
        this.restClient = restClientBuilder
                .baseUrl("https://jobs.autodesk.com")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Referer", "https://jobs.autodesk.com/careers")
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
            throw new IllegalStateException("Autodesk job search API returned an unsuccessful response");
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
                if (parser.matchesSearchPhrases(job, query)) {
                    matchingJobs.add(job);
                }
            }
        }
        return matchingJobs;
    }
}
