package com.jobscout.job;

import com.jobscout.company.Company;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "job_postings")
public class JobPosting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    private String externalJobId;
    @Column(nullable = false) private String title;
    private String location;
    @Column(length = 4000) private String description;
    @Column(length = 2048) private String jobUrl;
    private LocalDate postedDate;
    @Column(nullable = false) private Instant firstSeenAt = Instant.now();

    protected JobPosting() {}
    public Long getId(){return id;}
    public String getExternalJobId(){return externalJobId;}
    public String getTitle(){return title;}
    public String getLocation(){return location;}
    public String getJobUrl(){return jobUrl;}
    public LocalDate getPostedDate(){return postedDate;}
    public Instant getFirstSeenAt(){return firstSeenAt;}
}
