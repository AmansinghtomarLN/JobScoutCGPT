package com.jobscout.jobsearch;

import com.jobscout.configuration.CompanySearchConfig;
import com.jobscout.configuration.CompanySearchConfigRepository;
import com.jobscout.jobsearch.model.JobSyncResult;
import com.jobscout.jobsearch.service.AutodeskJobPersistenceService;
import com.jobscout.jobsearch.vodafone.VodafoneJobPersistenceService;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CompanyScanOrchestrator {

    private final CompanySearchConfigRepository configRepository;
    private final AutodeskJobPersistenceService autodeskService;
    private final VodafoneJobPersistenceService vodafoneService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "company-scan-worker");
        thread.setDaemon(true);
        return thread;
    });
    private final ConcurrentMap<String, ScanRun> scans = new ConcurrentHashMap<>();

    public CompanyScanOrchestrator(CompanySearchConfigRepository configRepository,
                                   AutodeskJobPersistenceService autodeskService,
                                   VodafoneJobPersistenceService vodafoneService) {
        this.configRepository = configRepository;
        this.autodeskService = autodeskService;
        this.vodafoneService = vodafoneService;
    }

    public Map<String, Object> startScanAll() {
        List<CompanySearchConfig> configs = configRepository.findAll();
        String scanId = UUID.randomUUID().toString();
        ScanRun run = new ScanRun(scanId, configs.size());
        for (CompanySearchConfig config : configs) {
            run.addCompany(config.getCompanyName(), blankToDefault(config.getSearchPhrases(), "Java"),
                    blankToDefault(config.getLocation(), "India"));
        }
        scans.put(scanId, run);
        executor.submit(() -> executeScan(run, configs));
        return run.snapshot();
    }

    public Map<String, Object> getProgress(String scanId) {
        ScanRun run = scans.get(scanId);
        if (run == null) {
            throw new NoSuchElementException("Scan not found: " + scanId);
        }
        return run.snapshot();
    }

    private void executeScan(ScanRun run, List<CompanySearchConfig> configs) {
        run.setStatus("RUNNING");
        for (CompanySearchConfig config : configs) {
            String company = config.getCompanyName();
            String query = blankToDefault(config.getSearchPhrases(), "Java");
            String location = blankToDefault(config.getLocation(), "India");
            String key = normalize(company);
            run.updateCompany(company, "SCANNING", "Search in progress", null);

            try {
                JobSyncResult result;
                if (key.contains("autodesk")) {
                    result = autodeskService.searchAndSave(query, location, 0);
                } else if (key.contains("vodafone")) {
                    result = vodafoneService.searchAndSave(query, location, 0);
                } else {
                    run.updateCompany(company, "UNSUPPORTED",
                            "No company-specific scanner is implemented yet.", null);
                    continue;
                }
                run.updateCompany(company, "COMPLETED", null, result);
            } catch (Exception exception) {
                run.updateCompany(company, "FAILED",
                        exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(), null);
            }
        }
        run.finish();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    private static final class ScanRun {
        private final String scanId;
        private final int totalCompanies;
        private final List<Map<String, Object>> results = new ArrayList<>();
        private String status = "QUEUED";
        private int completedCompanies;
        private int companiesScanned;
        private int companiesFailed;
        private int totalDiscovered;
        private int totalInserted;
        private int totalUpdated;
        private int totalSkipped;

        private ScanRun(String scanId, int totalCompanies) {
            this.scanId = scanId;
            this.totalCompanies = totalCompanies;
        }

        synchronized void addCompany(String company, String query, String location) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("company", company);
            row.put("query", query);
            row.put("location", location);
            row.put("status", "PENDING");
            results.add(row);
        }

        synchronized void setStatus(String status) {
            this.status = status;
        }

        synchronized void updateCompany(String company, String status, String message, JobSyncResult result) {
            for (Map<String, Object> row : results) {
                if (Objects.equals(row.get("company"), company)) {
                    row.put("status", status);
                    if (message != null) row.put("message", message);
                    if (result != null) {
                        row.put("discovered", result.discovered());
                        row.put("inserted", result.inserted());
                        row.put("updated", result.updated());
                        row.put("skipped", result.skipped());
                        companiesScanned++;
                        totalDiscovered += result.discovered();
                        totalInserted += result.inserted();
                        totalUpdated += result.updated();
                        totalSkipped += result.skipped();
                    } else if ("FAILED".equals(status)) {
                        companiesFailed++;
                    }
                    if (!"SCANNING".equals(status) && !"PENDING".equals(status)) completedCompanies++;
                    break;
                }
            }
        }

        synchronized void finish() {
            status = "COMPLETED";
        }

        synchronized Map<String, Object> snapshot() {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("scanId", scanId);
            response.put("status", status);
            response.put("companiesConfigured", totalCompanies);
            response.put("companiesCompleted", completedCompanies);
            response.put("companiesScanned", companiesScanned);
            response.put("companiesFailed", companiesFailed);
            response.put("totalDiscovered", totalDiscovered);
            response.put("totalInserted", totalInserted);
            response.put("totalUpdated", totalUpdated);
            response.put("totalSkipped", totalSkipped);
            response.put("progressPercent", totalCompanies == 0 ? 100 : (completedCompanies * 100 / totalCompanies));
            response.put("results", new ArrayList<>(results));
            return response;
        }
    }
}
