package com.jobscout.company;
import jakarta.validation.constraints.*;
public record CompanyRequest(
    @NotBlank @Size(max=80) String companyKey,
    @NotBlank String name,
    @NotBlank @Size(max=2048) String careerUrl,
    @NotNull InteractionType interactionType,
    boolean enabled
) {}
