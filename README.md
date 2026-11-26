# JobScout

Config-driven job crawler MVP built with Java 21, Spring Boot 3.5.7, MySQL 8, Playwright Java and a responsive static web UI.

## Project package

The full source package is provided separately as `JobScout_Full_Spring_Boot_MySQL_UI.zip`.

## Features

- Company and career-site configuration
- Excel workbook import for company, search, extraction and pagination settings
- Google Drive XLSX import using OAuth refresh-token credentials
- Playwright-based browser crawling with CSS selectors
- Search by keyword, location and posted-date criteria
- Result-card extraction, next-button pagination and job deduplication
- MySQL persistence for companies, configurations, runs and job postings
- Dashboard, run center, execution monitor, job detail and CSV export
- Docker Compose configuration for MySQL

## Technology

- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- MySQL 8
- Playwright Java / Chromium
- Apache POI
- HTML, CSS and JavaScript

## Important limitations

This is an MVP, not a universal adapter for every applicant-tracking system. Selectors must be inspected and configured per career portal. Infinite-scroll and cursor/API pagination, detail-page extraction, recurring schedules and cancellation controls are not implemented. Autodesk selectors in the sample workbook are placeholders and must be verified against the live site.

Use the crawler only on public pages you are authorized to access. Respect each site's terms, robots guidance, rate limits and access controls. The project does not bypass CAPTCHA or authentication.

## Run locally

1. Create a MySQL database named `jobscout`.
2. Configure the database connection environment variables described in the project configuration.
3. Install Maven and Playwright Chromium.
4. Run `mvn spring-boot:run`.
5. Open `http://localhost:8080`.

See the full project README and configuration files in the source ZIP for exact setup details.
