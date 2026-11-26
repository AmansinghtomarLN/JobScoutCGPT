package com.jobscout.job;

import com.jobscout.company.Company;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "job_postings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_company_external_id",
                columnNames = {"company_id", "external_job_id"}))
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "external_job_id", length = 120)
    private String externalJobId;

    @Column(nullable = false)
    private String title;

    private String location;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(length = 2048)
    private String jobUrl;

    private LocalDate postedDate;

    @Column(nullable = false)
    private Instant firstSeenAt = Instant.now();

    protected JobPosting() {}

    public JobPosting(Company company, String externalJobId, String title,
                      String location, String description, String jobUrl, LocalDate postedDate) {
        this.company = company;
        this.externalJobId = externalJobId;
        updateDetails(title, location, description, jobUrl, postedDate);
    }

    public void updateDetails(String title, String location, String description,
                              String jobUrl, LocalDate postedDate) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.jobUrl = jobUrl;
        this.postedDate = postedDate;
    }

    public Long getId(){return id;}
    public Company getCompany(){return company;}
    public String getExternalJobId(){return externalJobId;}
    public String getTitle(){return title;}
    public String getLocation(){return location;}
    public String getDescription(){return description;}
    public String getJobUrl(){return jobUrl;}
    public LocalDate getPostedDate(){return postedDate;}
    public Instant getFirstSeenAt(){return firstSeenAt;}
}
