package com.jobscout.configuration;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/google-sheets")
public class GoogleSheetsController {
    private final GoogleSheetsReaderService readerService;
    private final GoogleSheetSyncService syncService;
    private final CompanySearchConfigRepository configRepository;

    public GoogleSheetsController(GoogleSheetsReaderService readerService,
                                  GoogleSheetSyncService syncService,
                                  CompanySearchConfigRepository configRepository) {
        this.readerService = readerService;
        this.syncService = syncService;
        this.configRepository = configRepository;
    }

    @GetMapping("/{spreadsheetId}")
    public Map<String, Object> readSheet(
            @PathVariable String spreadsheetId,
            @RequestParam(defaultValue = "A2:F100") String range) {

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

    @PostMapping("/sync")
    public Map<String, Object> syncSheet(@Valid @RequestBody GoogleSheetSyncRequest request) {
        String range = request.range() == null || request.range().isBlank()
                ? "A2:F100" : request.range();
        return syncService.sync(request.spreadsheetId(), range);
    }

    @GetMapping("/configurations")
    public List<CompanySearchConfig> configurations() {
        return configRepository.findAll();
    }
}
