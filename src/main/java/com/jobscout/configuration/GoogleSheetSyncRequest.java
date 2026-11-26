package com.jobscout.configuration;

import jakarta.validation.constraints.NotBlank;

public record GoogleSheetSyncRequest(
        @NotBlank String spreadsheetId,
        String range
) {}
