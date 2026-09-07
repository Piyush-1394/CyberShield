# CyberShield AI

End-to-end cyber risk quantification and investment optimization app:

- **Backend**: Java 25, Spring Boot 3.3, Spring Security JWT (access + refresh), JPA/Hibernate, Flyway, PostgreSQL, springdoc OpenAPI, Razorpay test-mode tokenization behind `PaymentGateway`.
- **Frontend**: Vite + React 18, React Router, React Query, Axios. Access token stays in memory; refresh token is returned in the login body and also set as an `httpOnly` cookie.

## Quick start (Docker)

```bash
docker compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Postgres: `localhost:5432` / `cybershield` / `cybershield`

Then in another terminal:

```bash
cd frontend
npm install
npm run dev
```

UI: http://localhost:5173

## Local backend without Docker

Requires **JDK 25** on `PATH` (`java -version` should report 25). Maven is optional: this repo includes a wrapper.

1. Create a Postgres database named `cybershield`.
2. From `backend/`:

```bash
.\\mvnw.cmd spring-boot:run
```

Default profile is `dev` (`application-dev.yml`). Swap engines with:

- `h2` — in-memory (`-Dspring-boot.run.profiles=h2`)
- `prod` — `ddl-auto: validate` only (no schema auto-update); Flyway still runs

## Frontend environment

`frontend/.env`:

```
VITE_API_BASE_URL=http://localhost:8080
```

CORS origins are `CORS_ALLOWED_ORIGINS` (comma-separated). Default: `http://localhost:5173,http://127.0.0.1:5173`.

## Seeded logins

Password rules: 10+ characters, upper, lower, digit, special.

| Role    | Email                    | Password       | Org             |
|---------|--------------------------|----------------|-----------------|
| ADMIN   | admin@abc.university     | Admin#2026Ai   | ABC University  |
| ANALYST | analyst@abc.university   | Analyst#2026Ai | ABC University  |
| VIEWER  | viewer@abc.university    | Viewer#2026Ai  | ABC University  |
| ADMIN   | admin@other.edu          | Other#2026Ai   | Other College   |

Disable seeding with `APP_SEED=false`.

Self-serve **register** creates a new organization + first ADMIN. Register with an **invite token** (Settings → Team, or `POST /api/users/invite`) joins an existing org. Invite and password-reset tokens are logged by `LoggingEmailService` (swap `EmailService` for a real mailer).

## Auth

- Access JWT: HS256, 15 minutes, `Authorization: Bearer`
- Refresh: 14 days, stored hashed in `refresh_tokens`, rotatable, revocable on logout
- Roles: `ADMIN` (users + billing), `ANALYST` (write risk data), `VIEWER` (read-only)
- Tenancy: every query filters by `organization_id` from the JWT via `OrgGuard` / `@AssertOrgAccess`

## Risk score formula

Documented on `RiskComputationService`:

```
assetRisk = clamp(0–100,
    0.45 * vulnScore + 0.30 * threatScore + 0.25 * criticalityWeight)
  * (1 - appliedReduction)

vulnScore / threatScore / criticalityWeight:
  CRITICAL=100, HIGH=75, MEDIUM=45, LOW=20
  (open / in-progress vulns only; patched and accepted-risk ignored)

appliedReduction = min(0.70, sum of applied remediation_action.risk_reduction_percent)

orgOverall     = exposure-weighted average of assetRisk
financialExposure = sum(asset.financialExposure * assetRisk / 100)
```

Marking a vulnerability patched recalculates the org. Applying investment actions deducts budget and compounds residual risk (capped at 70% reduction).

What-if delay: `delayedExposure = current * (1 + min(0.35, delayDays/365 * 0.8))`.

Investment optimizer: greedy sort by `risk_reduction_percent / cost` within remaining budget (0-1 knapsack-style).

AI summary (`GET /api/risk/ai-summary`) is rules-based (`AiSummaryService`) — swap in an LLM later.

## Billing

Plans are Starter / Growth / Enterprise (₹ / month). Card/UPI methods are tokenized through `PaymentGateway`. If `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` are unset, a mock token is stored. Invoice and report PDFs use OpenPDF.

## Tests

```bash
cd backend
.\\mvnw.cmd test
```

- `RiskComputationServiceTest` — formula unit tests
- `IsolationAndRolesIT` — Testcontainers Postgres: Org A cannot see Org B; VIEWER cannot write; ANALYST cannot switch plan; ADMIN can invite

## API map

| Area | Prefix |
|------|--------|
| Auth | `/api/auth/*` |
| Org / users | `/api/organizations`, `/api/users` |
| Assets | `/api/assets` |
| Vulnerabilities | `/api/vulnerabilities` |
| Threat intel | `/api/threat-intel` |
| Risk | `/api/risk/overview`, `/distribution`, `/trend`, `/ai-summary` |
| Investment | `/api/investment/recommendations`, `/projection`, `/apply` |
| What-if | `/api/whatif/scenarios`, `/simulate` |
| Alerts | `/api/alerts` |
| Billing | `/api/billing/*` |
| Reports | `/api/reports` |
| Settings | `/api/settings/*` |
