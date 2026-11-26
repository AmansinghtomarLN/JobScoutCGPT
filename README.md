# JobScout – Full Spring Boot + UI Starter (MySQL)

This is a single Spring Boot application with a built-in web UI served from `src/main/resources/static`. You do not need to start a separate React development server.

## Requirements
- JDK 21
- Maven 3.9+
- MySQL 8+

## 1. Create the database

```sql
CREATE DATABASE jobscout;
```

Update `src/main/resources/application.yml` with your MySQL username and password, or set:

```bash
export DB_URL='jdbc:mysql://localhost:3306/jobscout?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your_mysql_password'
```

## 2. Run

From the project root:

```bash
mvn clean spring-boot:run
```

Open the UI:

**http://localhost:8080**

Health endpoint: `http://localhost:8080/actuator/health`

## UI pages included
- Dashboard
- Companies
- Run Center
- Job Results
- Run History
- Configuration (Excel upload and basic validation)
- Settings

The UI is responsive and calls the Spring Boot REST APIs from the same origin.

## Important implementation status

This is a runnable UI + backend foundation so you can inspect the pages and test basic CRUD/request flows. It is not yet the complete production crawler:
- Company registration is persisted to MySQL.
- Run requests are persisted to MySQL, but do not yet launch browser crawling.
- Jobs page reads persisted job records; no jobs are automatically scraped yet.
- Excel upload validates the Companies sheet headers and previews rows; it does not yet sync Google Drive or persist all configuration sheets.
- Google Drive OAuth integration is not included.
- Generic search actions, pagination crawler, extraction mappings, worker orchestration and result ingestion are the next implementation milestone.

The UI explicitly labels these unfinished areas so the demo does not imply that scraping has already run.

## API endpoints
- `GET /api/dashboard/summary`
- `GET, POST /api/companies`
- `GET /api/jobs`
- `GET, POST /api/runs`
- `POST /api/configurations/upload`

## Playwright
Playwright Java dependency is included. Browser automation is not yet wired to the Run Center button. Install Chromium when beginning crawler implementation:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## Security / compliance
Use this only for public pages and permitted endpoints. Respect website terms, robots.txt guidance, rate limits and access restrictions. Do not attempt CAPTCHA or authentication bypass.
