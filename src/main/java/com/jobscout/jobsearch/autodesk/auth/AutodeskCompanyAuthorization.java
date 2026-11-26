package com.jobscout.jobsearch.autodesk.auth;

import com.jobscout.company.Company;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "company_authorizations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_authorization_company",
                columnNames = "company_id"))
public class AutodeskCompanyAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(nullable = false)
    private boolean authorized;

    private Instant authorizedAt;

    private Instant revokedAt;

    protected AutodeskCompanyAuthorization() {
    }

    public AutodeskCompanyAuthorization(Company company) {
        this.company = company;
        this.authorized = false;
    }

    public void authorize() {
        this.authorized = true;
        this.authorizedAt = Instant.now();
        this.revokedAt = null;
    }

    public void revoke() {
        this.authorized = false;
        this.revokedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public Instant getAuthorizedAt() {
        return authorizedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getCompanyKey() {
        return company.getCompanyKey();
    }

    public String getCompanyName() {
        return company.getName();
    }
}
