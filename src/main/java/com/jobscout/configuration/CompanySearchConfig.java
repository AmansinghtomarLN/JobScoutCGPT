package com.jobscout.configuration;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "company_search_configs", uniqueConstraints =
        @UniqueConstraint(name = "uk_search_config_company", columnNames = "company_name"))
public class CompanySearchConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", length = 30)
    private String serialNumber;

    @Column(name = "company_name", nullable = false, unique = true, length = 150)
    private String companyName;

    @Column(name = "company_search_url", nullable = false, length = 2048)
    private String companySearchUrl;

    @Column(name = "search_phrases", length = 1000)
    private String searchPhrases;

    @Column(length = 150)
    private String location;

    @Column(name = "posted_date_days", length = 100)
    private String postedDateDays;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected CompanySearchConfig() {}

    public CompanySearchConfig(String serialNumber, String companyName, String companySearchUrl,
                               String searchPhrases, String location, String postedDateDays) {
        update(serialNumber, companyName, companySearchUrl, searchPhrases, location, postedDateDays);
    }

    public void update(String serialNumber, String companyName, String companySearchUrl,
                       String searchPhrases, String location, String postedDateDays) {
        this.serialNumber = serialNumber;
        this.companyName = companyName;
        this.companySearchUrl = companySearchUrl;
        this.searchPhrases = searchPhrases;
        this.location = location;
        this.postedDateDays = postedDateDays;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSerialNumber() { return serialNumber; }
    public String getCompanyName() { return companyName; }
    public String getCompanySearchUrl() { return companySearchUrl; }
    public String getSearchPhrases() { return searchPhrases; }
    public String getLocation() { return location; }
    public String getPostedDateDays() { return postedDateDays; }
    public Instant getUpdatedAt() { return updatedAt; }
}
