package com.jobscout.configuration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/google-sheets")
public class GoogleSheetsController {
    private final GoogleSheetsReaderService readerService;

    public GoogleSheetsController(GoogleSheetsReaderService readerService) {
        this.readerService = readerService;
    }

    /**
     * Reads values from a Google spreadsheet shared with the configured service account.
     *
     * Example:
     * GET /api/google-sheets/17mE6oEp49N3Ihb-jgT-3x0eL-Gsyq_BZwI6QeshoBo4?range=Companies!A1:Z100
     */
    @GetMapping("/{spreadsheetId}")
    public Map<String, Object> readSheet(
            @PathVariable String spreadsheetId,
            @RequestParam(defaultValue = "Companies!A1:Z100") String range) {

        Map<String, Object> sheetData = readerService.readValues(spreadsheetId, range);
        List<List<Object>> values = (List<List<Object>>) sheetData.getOrDefault("values", List.of());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("spreadsheetId", spreadsheetId);
        response.put("range", sheetData.getOrDefault("range", range));
        response.put("rowCount", values.size());
        response.put("values", values);
        response.put("message", "Google Sheet read successfully.");
        return response;
    }
}
