package com.jobscout.configuration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GoogleSheetSyncService {
    private final GoogleSheetsReaderService reader;
    private final CompanySearchConfigRepository repository;

    public GoogleSheetSyncService(GoogleSheetsReaderService reader, CompanySearchConfigRepository repository) {
        this.reader = reader;
        this.repository = repository;
    }

    @Transactional
    public Map<String, Object> sync(String spreadsheetId, String range) {
        Map<String, Object> response = reader.readValues(spreadsheetId, range);
        Object raw = response.get("values");
        if (!(raw instanceof List<?> rows) || rows.isEmpty()) {
            return result(0, 0, 0, List.of(), "No rows found in the selected range.");
        }

        List<?> headerRow = (List<?>) rows.get(0);
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String header = cell(headerRow, i).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            columns.put(header, i);
        }

        int companyCol = column(columns, "companyname");
        int urlCol = column(columns, "companysearchurl");
        int serialCol = column(columns, "sno", "serialnumber");
        int phrasesCol = column(columns, "searchphrases");
        int locationCol = column(columns, "location");
        int postedCol = column(columns, "posteddatedays", "posteddateday");

        if (companyCol < 0 || urlCol < 0) {
            throw new IllegalArgumentException("Sheet must include Company Name and Company Search Url headers.");
        }

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        List<String> syncedCompanies = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            if (!(rows.get(i) instanceof List<?> row)) { skipped++; continue; }
            String companyName = cell(row, companyCol);
            String searchUrl = cell(row, urlCol);
            if (companyName.isBlank() || searchUrl.isBlank()) { skipped++; continue; }

            String serial = cell(row, serialCol);
            String phrases = cell(row, phrasesCol);
            String location = cell(row, locationCol);
            String posted = cell(row, postedCol);

            var existing = repository.findByCompanyNameIgnoreCase(companyName);
            if (existing.isPresent()) {
                existing.get().update(serial, companyName, searchUrl, phrases, location, posted);
                updated++;
            } else {
                repository.save(new CompanySearchConfig(serial, companyName, searchUrl, phrases, location, posted));
                inserted++;
            }
            syncedCompanies.add(companyName);
        }
        return result(inserted, updated, skipped, syncedCompanies, "Google Sheet configuration synced to MySQL.");
    }

    private Map<String, Object> result(int inserted, int updated, int skipped, List<String> companies, String message) {
        return Map.of("inserted", inserted, "updated", updated, "skipped", skipped,
                "totalSynced", inserted + updated, "companies", companies, "message", message);
    }

    private int column(Map<String, Integer> columns, String... names) {
        for (String name : names) if (columns.containsKey(name)) return columns.get(name);
        return -1;
    }

    private String cell(List<?> row, int index) {
        if (index < 0 || index >= row.size() || row.get(index) == null) return "";
        return String.valueOf(row.get(index)).trim();
    }
}
