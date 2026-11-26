package com.jobscout.jobsearch.autodesk.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutodeskCompanyAuthorizationRepository
        extends JpaRepository<AutodeskCompanyAuthorization, Long> {

    Optional<AutodeskCompanyAuthorization> findByCompanyCompanyKey(String companyKey);
}
