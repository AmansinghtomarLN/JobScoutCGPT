package com.jobscout.run;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "execution_runs")
public class ExecutionRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String companyKey;
    private String keyword;
    private String location;
    private Integer postedWithinDays;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private ExecutionStatus status = ExecutionStatus.QUEUED;
    @Column(nullable=false) private Instant createdAt = Instant.now();

    protected ExecutionRun() {}
    public ExecutionRun(String companyKey, String keyword, String location, Integer postedWithinDays) {
        this.companyKey=companyKey; this.keyword=keyword; this.location=location; this.postedWithinDays=postedWithinDays;
    }
    public Long getId(){return id;}
    public String getCompanyKey(){return companyKey;}
    public String getKeyword(){return keyword;}
    public String getLocation(){return location;}
    public Integer getPostedWithinDays(){return postedWithinDays;}
    public ExecutionStatus getStatus(){return status;}
    public Instant getCreatedAt(){return createdAt;}
}
