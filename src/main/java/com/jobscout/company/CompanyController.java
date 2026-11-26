package com.jobscout.company;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository repository;
    public CompanyController(CompanyRepository repository) { this.repository = repository; }

    @GetMapping public List<Company> list() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Company create(@Valid @RequestBody CompanyRequest request) {
        if (repository.findByCompanyKey(request.companyKey()).isPresent()) {
            throw new IllegalArgumentException("Company key already exists: " + request.companyKey());
        }
        return repository.save(new Company(request.companyKey(), request.name(), request.careerUrl(),
                request.interactionType(), request.enabled()));
    }
}
