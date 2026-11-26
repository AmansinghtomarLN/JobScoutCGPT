package com.jobscout.company;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "companies", uniqueConstraints = @UniqueConstraint(name = "uk_company_key", columnNames = "company_key"))
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_key", nullable = false, unique = true, length = 80)
    private String companyKey;
    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 2048) private String careerUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private InteractionType interactionType = InteractionType.PLAYWRIGHT;
    @Column(nullable = false) private boolean enabled = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected Company() {}
    public Company(String companyKey, String name, String careerUrl, InteractionType interactionType, boolean enabled) {
        this.companyKey = companyKey; this.name = name; this.careerUrl = careerUrl;
        this.interactionType = interactionType; this.enabled = enabled;
    }
    public Long getId(){ return id; }
    public String getCompanyKey(){ return companyKey; }
    public String getName(){ return name; }
    public String getCareerUrl(){ return careerUrl; }
    public InteractionType getInteractionType(){ return interactionType; }
    public boolean isEnabled(){ return enabled; }
    public Instant getCreatedAt(){ return createdAt; }
}
