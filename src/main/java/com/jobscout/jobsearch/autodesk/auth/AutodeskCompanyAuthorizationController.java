package com.jobscout.jobsearch.autodesk.auth;

import com.jobscout.company.Company;
import com.jobscout.company.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/autodesk/authorization")
public class AutodeskCompanyAuthorizationController {

    private static final String COMPANY_KEY = "AUTODESK";

    private final CompanyRepository companyRepository;
    private final AutodeskCompanyAuthorizationRepository authorizationRepository;

    public AutodeskCompanyAuthorizationController(
            CompanyRepository companyRepository,
            AutodeskCompanyAuthorizationRepository authorizationRepository) {
        this.companyRepository = companyRepository;
        this.authorizationRepository = authorizationRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public AuthorizationResponse status() {
        return authorizationRepository.findByCompanyCompanyKey(COMPANY_KEY)
                .map(this::toResponse)
                .orElseGet(() -> new AuthorizationResponse(
                        COMPANY_KEY, "Autodesk", false, null, null));
    }

    /**
     * Records the user's company-level approval for JobScout to access
     * the Autodesk candidate portal on their behalf.
     * This does not approve or submit any individual job application.
     */
    @PostMapping
    @Transactional
    public AuthorizationResponse authorize() {
        Company company = companyRepository.findByCompanyKey(COMPANY_KEY)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Autodesk company is not registered. Run Autodesk job sync first."));

        AutodeskCompanyAuthorization authorization =
                authorizationRepository.findByCompanyCompanyKey(COMPANY_KEY)
                        .orElseGet(() -> new AutodeskCompanyAuthorization(company));

        authorization.authorize();
        return toResponse(authorizationRepository.save(authorization));
    }

    @DeleteMapping
    @Transactional
    public AuthorizationResponse revoke() {
        AutodeskCompanyAuthorization authorization =
                authorizationRepository.findByCompanyCompanyKey(COMPANY_KEY)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "No Autodesk authorization exists."));

        authorization.revoke();
        return toResponse(authorizationRepository.save(authorization));
    }

    private AuthorizationResponse toResponse(AutodeskCompanyAuthorization authorization) {
        return new AuthorizationResponse(
                authorization.getCompanyKey(),
                authorization.getCompanyName(),
                authorization.isAuthorized(),
                authorization.getAuthorizedAt(),
                authorization.getRevokedAt());
    }

    public record AuthorizationResponse(
            String companyKey,
            String companyName,
            boolean authorized,
            Instant authorizedAt,
            Instant revokedAt) {
    }
}
