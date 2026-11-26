package com.jobscout.jobsearch;

import com.jobscout.configuration.CompanySearchConfig;
import com.jobscout.configuration.CompanySearchConfigRepository;
import com.jobscout.jobsearch.model.JobSyncResult;
import com.jobscout.jobsearch.service.AutodeskJobPersistenceService;
import com.jobscout.jobsearch.vodafone.VodafoneJobPersistenceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CompanyScanOrchestrator {

    private final CompanySearchConfigRepository configRepository;
    private final AutodeskJobPersistenceService autodeskService;
    private final VodafoneJobPersistenceService vodafoneService;

    public CompanyScanOrchestrator(CompanySearchConfigRepository configRepository,
                                   AutodeskJobPersistenceService autodeskService,
                                   VodafoneJobPersistenceService vodafoneService) {
        this.configRepository = configRepository;
        this.autodeskService = autodeskService;
        this.vodafoneService = vodafoneService;
    }

    public Map<String, Object> scanAll() {
        List<CompanySearchConfig> configs = configRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();
        int scanned = 0, discovered = 0, inserted = 0, updated = 0, skipped = 0, failed = 0;

        for (CompanySearchConfig config : configs) {
            String company = config.getCompanyName();
            String query = blankToDefault(config.getSearchPhrases(), "Java");
            String location = blankToDefault(config.getLocation(), "India");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("company", company);
            row.put("query", query);
            row.put("location", location);

            try {
                JobSyncResult result;
                String key = company.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
                if (key.contains("autodesk")) {
                    result = autodeskService.searchAndSave(query, location, 0);
                } else if (key.contains("vodafone")) {
                    result = vodafoneService.searchAndSave(query, location, 0);
                } else {
                    row.put("status", "UNSUPPORTED");
                    row.put("message", "No company-specific scanner is implemented yet.");
                    results.add(row);
                    continue;
                }

                scanned++;
                discovered += result.discovered();
                inserted += result.inserted();
                updated += result.updated();
                skipped += result.skipped();
                row.put("status", "COMPLETED");
                row.put("discovered", result.discovered());
                row.put("inserted", result.inserted());
                row.put("updated", result.updated());
                row.put("skipped", result.skipped());
            } catch (Exception exception) {
                failed++;
                row.put("status", "FAILED");
                row.put("message", exception.getMessage());
            }
            results.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("companiesConfigured", configs.size());
        response.put("companiesScanned", scanned);
        response.put("companiesFailed", failed);
        response.put("totalDiscovered", discovered);
        response.put("totalInserted", inserted);
        response.put("totalUpdated", updated);
        response.put("totalSkipped", skipped);
        response.put("results", results);
        return response;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
