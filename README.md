# JobScout – Spring Boot + MySQL UI Starter

A single Spring Boot application with a built-in web UI served from `src/main/resources/static`.

## Requirements
- JDK 21
- Maven 3.9+
- MySQL 8+

## Run locally

Create the database:

```sql
CREATE DATABASE jobscout;
```

Configure the database:

```bash
export DB_URL='jdbc:mysql://localhost:3306/jobscout?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your_mysql_password'
```

Run from the project root:

```bash
mvn clean spring-boot:run
```

The current `application.yml` uses port **8082**. Open http://localhost:8082.

## Read a Google Sheet

JobScout exposes a read-only Google Sheets API endpoint. It uses a Google Cloud service account and the Sheets API read-only scope. It does not require a public spreadsheet link or a user's personal Google password.

### 1. Enable Google Sheets API and create a service account

1. Open https://console.cloud.google.com/.
2. Create or select a Google Cloud project.
3. Open **APIs & Services → Library**, search for **Google Sheets API**, and click **Enable**.
4. Open **IAM & Admin → Service Accounts** and create a service account (for example, `jobscout-sheets-reader`).
5. Open that service account → **Keys → Add key → Create new key → JSON**.
6. Download the JSON key. Keep it private; do not commit it to GitHub or share it in chat.

### 2. Give the service account access to your spreadsheet

1. Open the Google Sheet in your browser.
2. In the downloaded JSON file, find the `client_email` field. It looks like `jobscout-sheets-reader@your-project.iam.gserviceaccount.com`.
3. In Google Sheets, click **Share**.
4. Paste that exact service-account email address.
5. Grant **Viewer** access and click **Send/Share**.

The spreadsheet can remain restricted. You do not need to enable “Anyone with the link”.

### 3. Configure the key on the machine running JobScout

On macOS/Linux, keep the JSON outside the Git repository, for example:

```bash
mkdir -p "$HOME/.jobscout/secrets"
mv "$HOME/Downloads/your-service-account-key.json" "$HOME/.jobscout/secrets/google-sheets-reader.json"
chmod 600 "$HOME/.jobscout/secrets/google-sheets-reader.json"
export GOOGLE_SERVICE_ACCOUNT_FILE="$HOME/.jobscout/secrets/google-sheets-reader.json"
```

Start/restart JobScout in the same terminal so it receives the environment variable:

```bash
mvn spring-boot:run
```

For deployment, store the JSON key in your platform's secret manager and mount it as a file; set `GOOGLE_SERVICE_ACCOUNT_FILE` to that mounted file path. Never add the key to `application.yml`, source control, Docker image layers, or logs.

### 4. Call the read endpoint

Spreadsheet ID from this URL:

`https://docs.google.com/spreadsheets/d/17mE6oEp49N3Ihb-jgT-3x0eL-Gsyq_BZwI6QeshoBo4/edit`

ID:

`17mE6oEp49N3Ihb-jgT-3x0eL-Gsyq_BZwI6QeshoBo4`

Example (assuming the tab is named `Companies`):

```bash
curl --get "http://localhost:8082/api/google-sheets/17mE6oEp49N3Ihb-jgT-3x0eL-Gsyq_BZwI6QeshoBo4" \
  --data-urlencode "range=Companies!A1:Z100"
```

Change `Companies` to the exact worksheet tab name at the bottom of your Google Sheet. You can also request a different range, for example `SearchFields!A1:Z100`.

Response contains the resolved range, row count and cell values as JSON.

## New API

`GET /api/google-sheets/{spreadsheetId}?range=Companies!A1:Z100`

- Reads cell values from a Google spreadsheet.
- Uses service-account credentials from `GOOGLE_SERVICE_ACCOUNT_FILE`.
- Requests read-only Sheets scope.
- Returns raw cell values; it does **not yet** import them into MySQL or automatically map them to JobScout company/configuration entities.

Existing APIs include:
- `GET /api/dashboard/summary`
- `GET, POST /api/companies`
- `GET /api/jobs`
- `GET, POST /api/runs`
- `POST /api/configurations/upload`

## Current implementation status

The repository is a UI + backend starter, not a complete production crawler:
- Company registration and run requests are persisted to MySQL.
- The current Excel upload validates the Companies sheet headers and previews rows; it does not sync all workbook tabs to MySQL.
- The Run Center does not yet launch browser crawling; jobs are not automatically scraped.
- The Google Sheets endpoint added here reads a requested A1 range. A follow-up mapping/import flow is needed to persist the returned sheet data as JobScout configuration.
- Playwright is included, but browser automation is not yet wired to the Run Center.

## Playwright

When beginning crawler implementation, install Chromium:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## Security and compliance

Use the crawler only on public pages and permitted endpoints. Respect website terms, robots.txt guidance, rate limits and access restrictions. Do not attempt CAPTCHA or authentication bypass. Treat service-account JSON keys as passwords.


## Google Sheets company configuration sync

The **Google Sheet** page in the web UI previews the live worksheet and syncs company search settings into MySQL.

- Open the application and select **Google Sheet** in the sidebar.
- Enter the spreadsheet ID (the default is the configured JobScout sheet).
- Select **Load sheet** to preview rows from `A2:F100` (headers in row 2, data from row 3).
- Select **Sync to MySQL** to insert new companies or update existing rows matched by company name.
- The UI shows the live sheet preview and the saved MySQL configurations.

Expected columns: `S.No`, `Company Name`, `Company Search Url`, `Search phrases`, `Location`, `Posted Date/Days`. The first worksheet is used when the range has no tab prefix.

API endpoints:
- `GET /api/google-sheets/{spreadsheetId}?range=A2:F100` — read sheet values.
- `POST /api/google-sheets/sync` with JSON `{"spreadsheetId":"...","range":"A2:F100"}` — upsert rows into `company_search_configs`.
- `GET /api/google-sheets/configurations` — list saved configurations.

This sync stores search configuration only; it does not launch the crawler.
