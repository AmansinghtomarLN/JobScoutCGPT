package com.jobscout.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanySearchConfigRepository extends JpaRepository<CompanySearchConfig, Long> {
    Optional<CompanySearchConfig> findByCompanyNameIgnoreCase(String companyName);
}
