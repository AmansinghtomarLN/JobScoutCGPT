package com.jobscout.run;
import jakarta.validation.constraints.*;
public record CreateRunRequest(@NotBlank String companyKey, String keyword, String location,
                               @PositiveOrZero Integer postedWithinDays) {}
