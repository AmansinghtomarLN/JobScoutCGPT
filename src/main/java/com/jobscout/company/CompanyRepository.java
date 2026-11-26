package com.jobscout.company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyKey(String companyKey);
    long countByEnabledTrue();
}
