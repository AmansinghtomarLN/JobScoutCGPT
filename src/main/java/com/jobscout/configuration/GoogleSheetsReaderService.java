package com.jobscout.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSheetsReaderService {
    private static final String SHEETS_READONLY_SCOPE =
            "https://www.googleapis.com/auth/spreadsheets.readonly";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String credentialsFile;

    public GoogleSheetsReaderService(
            ObjectMapper objectMapper,
            @Value("${jobscout.google.sheets.credentials-file:}") String credentialsFile) {
        this.objectMapper = objectMapper;
        this.credentialsFile = credentialsFile;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> readValues(String spreadsheetId, String range) {
        validateSpreadsheetId(spreadsheetId);
        if (range == null || range.isBlank() || range.length() > 200) {
            throw new IllegalArgumentException("A valid A1 range is required, e.g. Companies!A1:Z100");
        }
        try {
            GoogleCredentials credentials = loadCredentials();
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();

            String encodedRange = URLEncoder.encode(range, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            URI uri = URI.create("https://sheets.googleapis.com/v4/spreadsheets/"
                    + spreadsheetId + "/values/" + encodedRange);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 403) {
                throw new IllegalStateException(
                        "Google denied access. Share the spreadsheet with the service-account email "
                                + "from the JSON key and grant Viewer access. Also ensure Google Sheets API is enabled.");
            }
            if (response.statusCode() == 404) {
                throw new IllegalArgumentException(
                        "Spreadsheet or range was not found. Check the spreadsheet ID and tab/range name.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Google Sheets API returned HTTP "
                        + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(),
                    new TypeReference<Map<String, Object>>() { });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Google service-account credentials or parse the Sheets response.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Google Sheets request was interrupted.", e);
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (credentialsFile == null || credentialsFile.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_SERVICE_ACCOUNT_FILE is not configured. Set it to the service-account JSON key file path.");
        }
        Path keyPath = Path.of(credentialsFile).toAbsolutePath().normalize();
        if (!Files.isRegularFile(keyPath) || !Files.isReadable(keyPath)) {
            throw new IllegalStateException("Google service-account JSON file is missing or unreadable: " + keyPath);
        }
        try (InputStream input = Files.newInputStream(keyPath)) {
            return GoogleCredentials.fromStream(input).createScoped(List.of(SHEETS_READONLY_SCOPE));
        }
    }

    private void validateSpreadsheetId(String spreadsheetId) {
        if (spreadsheetId == null || !spreadsheetId.matches("[A-Za-z0-9_-]{20,}")) {
            throw new IllegalArgumentException("Invalid Google spreadsheet ID.");
        }
    }
}
