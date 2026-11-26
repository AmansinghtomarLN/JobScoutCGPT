package com.jobscout.jobsearch.parser.autodesk;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobscout.jobsearch.model.DiscoveredJob;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class AutodeskJobParser {

    private static final String COMPANY_NAME = "Autodesk";
    private static final ZoneId POSTING_DATE_ZONE = ZoneId.of("Asia/Kolkata");

    public List<JsonNode> extractPositions(JsonNode searchResponse) {
        JsonNode positions = searchResponse.path("data").path("positions");
        if (!positions.isArray()) {
            return List.of();
        }

        List<JsonNode> result = new ArrayList<>();
        positions.forEach(result::add);
        return result;
    }

    public DiscoveredJob parseJob(JsonNode detailsResponse) {
        JsonNode job = detailsResponse.path("data");
        if (job.isMissingNode() || job.isNull() || job.isEmpty()) {
            throw new IllegalArgumentException("Autodesk job details response does not contain data");
        }

        String descriptionHtml = job.path("jobDescription").asText("");
        String descriptionText = Jsoup.parse(descriptionHtml).text();

        List<String> locations = new ArrayList<>();
        JsonNode locationNodes = job.path("locations");
        if (locationNodes.isArray()) {
            locationNodes.forEach(node -> {
                String value = node.asText("").trim();
                if (!value.isBlank() && !locations.contains(value)) {
                    locations.add(value);
                }
            });
        }
        if (locations.isEmpty() && job.hasNonNull("location")) {
            locations.add(job.path("location").asText());
        }

        LocalDate postedDate = null;
        long postedTs = job.path("postedTs").asLong(0);
        if (postedTs > 0) {
            postedDate = Instant.ofEpochSecond(postedTs)
                    .atZone(POSTING_DATE_ZONE)
                    .toLocalDate();
        }

        String jobUrl = job.path("publicUrl").asText("");
        if (jobUrl.isBlank()) {
            String positionUrl = job.path("positionUrl").asText("");
            if (!positionUrl.isBlank()) {
                jobUrl = positionUrl.startsWith("http")
                        ? positionUrl
                        : "https://jobs.autodesk.com" + positionUrl;
            }
        }

        return new DiscoveredJob(
                COMPANY_NAME,
                firstNonBlank(job.path("atsJobId").asText(""), job.path("displayJobId").asText(""),
                        job.path("id").asText("")),
                job.path("name").asText(""),
                String.join(" | ", locations),
                descriptionText,
                jobUrl,
                postedDate,
                job.path("workLocationOption").asText("")
        );
    }

    public boolean matchesSearchPhrases(DiscoveredJob job, String searchPhrases) {
        if (searchPhrases == null || searchPhrases.isBlank()) {
            return true;
        }

        String searchableText = (job.title() + " " + job.description()).toLowerCase(Locale.ROOT);
        return List.of(searchPhrases.split("[,;]")).stream()
                .map(String::trim)
                .filter(phrase -> !phrase.isBlank())
                .anyMatch(phrase -> searchableText.contains(phrase.toLowerCase(Locale.ROOT)));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
