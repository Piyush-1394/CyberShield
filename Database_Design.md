# 🗄️ Chapter 6: Database Design — The Unbreakable Digital Filing Cabinet

> *"A building is only as tall as its foundation is deep. In the world of software, code is just temporary choreography—the database is the permanent ground upon which all truth rests."*  
> — Welcome to Chapter 6! Today, we put on our architectural hard hats and step into the data vault of CyberShield.

---

## 1. What Are We Talking About?

**Database Design is the mathematical and logical blueprint of how CyberShield stores, links, organizes, and protects every piece of business information—from passwords and university servers to ransomware alerts and financial budgets.**

If you have ever used an Excel spreadsheet, you know that you can type anything into any cell. But when a software system must protect **millions of dollars of cyber assets**, handle **multi-million rupee financial risk models**, and guarantee that **Customer A can NEVER see Customer B's confidential vulnerabilities**, a casual spreadsheet is a recipe for catastrophe.

We need an industrial-grade **Relational Database Management System (RDBMS)**. CyberShield uses **PostgreSQL 16** (backed by **H2** for rapid in-memory local testing), managed through **Flyway versioned migration scripts**.

---

## 2. Why Do We Need a Thoughtful Database Design?

Imagine a hospital where patient records are scribbled on random sticky notes and tossed into a giant laundry basket:
* When a patient rushes into the emergency room allergic to penicillin, the nurse has to dig through 10,000 crumpled sticky notes!
* If a note gets coffee spilled on it, a patient's blood type is lost forever.
* Worse, if two patients have the same name *"Rahul Sharma"*, a doctor might perform surgery on the wrong person!

In software engineering, a poorly designed database causes:
1. **Data Corruption**: A vulnerability points to an asset ID that was deleted last week (an *orphan record*).
2. **Financial Rounding Errors**: A floating-point number rounds ₹10,00,000.45 down to ₹10,00,000.40, causing financial audits to fail.
3. **Catastrophic Data Leaks**: A hacker queries *"give me all vulnerabilities"* and the system accidentally leaks another company's confidential network weaknesses!

A **Relational Database** solves all three problems by enforcing strict **Constraints**, **Foreign Key Relationships**, and **Data Isolation Rules**.

---

## 3. Imagine This: The Grand Hotel & The Iron Vaults 🏨

To easily grasp how our database is structured, imagine a world-famous hotel called **The Cyber Grand**:

```
+-----------------------------------------------------------------------------------------+
|                                    THE CYBER GRAND                                      |
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|   [TENANT SUITE 101: ABC University]                [TENANT SUITE 102: Apex Healthcare] |
|   +---------------------------------------+         +---------------------------------+ |
|   | • Staff: Priya (Admin), Rohan (Viewer)|         | • Staff: Dr. Mehta (Admin)      | |
|   | • Property: 15 Servers, 2 Web Portals |         | • Property: MRI Scanners, DB    | |
|   | • Risk Wallet: ₹50,00,000             |         | • Risk Wallet: ₹1,20,00,000     | |
|   | • Vault Key: Org #1                   |         | • Vault Key: Org #2             | |
|   +---------------------------------------+         +---------------------------------+ |
|                       |                                              |                  |
|                       +----------------------+-----------------------+                  |
|                                              |                                          |
|                                              v                                          |
|                         [THE MASTER BASEMENT VAULT ROOM]                                |
|                         +------------------------------+                                |
|                         |   POSTGRESQL 16 SAFE ROOM   |                                |
|                         |  • 20 Steel Safe Cabinets    |                                |
|                         |  • Biometric Keycard Check   |                                |
|                         |  • Unbreakable Acid Proof    |                                |
|                         +------------------------------+                                |
+-----------------------------------------------------------------------------------------+
```

Notice the golden rules of the hotel:
1. **Every Suite is an Organization (Multi-Tenancy)**: ABC University lives in Suite 101; Apex Healthcare lives in Suite 102.
2. **Every Safe Locker is Tagged with a Suite Number (`organization_id`)**: When Priya from ABC University asks for a list of servers, the concierge looks *only* inside lockers stamped with `Suite #1`. Even if Priya tries to ask for Locker #102, the concierge immediately rings the alarm!
3. **Strict Compartments (Normalized Tables)**: Passwords don't live on the reception desk; they are locked inside the security locker. Invoices are stored in the accounting locker. Server details are stored in the asset locker.

---

## 4. The Complete Entity-Relationship (ER) Architecture

Here is the grand bird's-eye map of all 20 tables in CyberShield and how they connect to one another:

```
+----------------------------------------------------------------------------------------------------+
|                                  CYBERSHIELD DATABASE ECOSYSTEM                                    |
+----------------------------------------------------------------------------------------------------+

   [ORGANIZATIONS] (Tenant Root)
         |
         +---+--------------------+-----------------------+---------------------+
         |   |                    |                       |                     |
         v   v                    v                       v                     v
     [USERS]                [ASSETS]                [BILLING_PLANS]      [RISK_SNAPSHOTS]
        |                      |                          |                     |
        +--> [REFRESH_TOKENS]  +--> [VULNERABILITIES]     v                     v
        |                      |         |        [ORG_SUBSCRIPTIONS]    (Historical Trends)
        +--> [RESET_TOKENS]    |         +--> [ALERTS]    |
        |                      |                 ^        +--> [PAYMENT_METHODS]
        +--> [INVITES]         v                 |        |
        |               [THREAT_INTEL]           |        +--> [INVOICES]
        +--> [NOTIF_PREFS]     |                 |
                               +--> [THREAT_INTEL_ASSETS] (Junction)
                               
   [REMEDIATION_ACTIONS] (Security Controls)
         |
         +--> [APPLIED_INVESTMENTS] (Budget Spent)
         |
         +--> [WHATIF_SCENARIOS] (Simulation Sandbox)
         
   [REPORTS] (Executive PDFs / CSVs)
```

---

## 5. Deep-Dive: The 6 Core Database Clusters

Let's open each of the 6 major clusters and examine their exact database tables, column types, and design decisions.

---

### Cluster 1: The Multi-Tenant & Identity Vault

This cluster manages organizations, team members, invitations, and cryptographic authentication tokens.

```
+------------------+         +------------------+         +-----------------------+
|  ORGANIZATIONS   | 1     * |      USERS       | 1     * |    REFRESH_TOKENS     |
|------------------|<-------+|------------------|<-------+|-----------------------|
| PK id            |         | PK id            |         | PK id                 |
|    name          |         | FK org_id        |         | FK user_id (CASCADE)  |
|    budget_avail  |         |    email (UNIQUE)|         |    token_hash (UNIQUE)|
|    budget_alloc  |         |    password_hash |         |    expires_at         |
|    created_at    |         |    full_name     |         |    revoked            |
+------------------+         |    role          |         +-----------------------+
                             +------------------+
```

#### 1. `organizations` (The Root Tenant)
Every customer entity (university, hospital, bank) starts with a row here.
* `id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY`: Auto-incrementing 64-bit integer.
* `name VARCHAR(160) NOT NULL`: Human-readable name (e.g., *"ABC University"*).
* `budget_available NUMERIC(19, 2) NOT NULL DEFAULT 0`: The remaining cyber budget available for allocation.
* `budget_allocated NUMERIC(19, 2) NOT NULL DEFAULT 0`: The cyber budget already committed to security controls.

> 🎓 **Professor's Note: Why `NUMERIC(19, 2)` instead of `FLOAT` or `DOUBLE`?**  
> Computers store floating-point numbers in binary scientific notation (IEEE 754). In standard `DOUBLE`, `0.1 + 0.2` equals `0.30000000000000004`! In finance and cyber risk calculations, that four-quadrillionth discrepancy creates auditing nightmares. `NUMERIC(19, 2)` stores numbers as exact fixed-point decimal digits—guaranteeing 100% precision down to the last paisa or cent.

#### 2. `users` (Account Identities)
* `organization_id BIGINT REFERENCES organizations(id)`: Establishes tenant ownership.
* `email VARCHAR(180) UNIQUE NOT NULL`: The login handle.
* `password_hash VARCHAR(120) NOT NULL`: BCrypt hash (60 characters, e.g. `$2a$10$...`). Never store raw passwords!
* `role VARCHAR(32) NOT NULL`: Role-Based Access Control (`ADMIN`, `ANALYST`, `VIEWER`).

#### 3. `refresh_tokens` & `password_reset_tokens` (Crypto Token Storage)
* `token_hash VARCHAR(128) NOT NULL UNIQUE`: Stores the **SHA-256 hash** of the token, not the plaintext token. Even if a rogue DBA reads the database, they cannot use the leaked hashes to authenticate!
* `ON DELETE CASCADE`: If a user account is deleted, all their active sessions and reset tokens are instantly and automatically vaporized by the database.

---

### Cluster 2: The Asset & Vulnerability Vault

This cluster holds the digital balance sheet of the organization.

```
+------------------+ 1     * +------------------------+
|      ASSETS      |--------<|    VULNERABILITIES     |
|------------------|         |------------------------|
| PK id            |         | PK id                  |
| FK org_id        |         | FK org_id              |
|    name          |         | FK asset_id            |
|    type          |         |    title               |
|    category      |         |    severity (CVSS)     |
|    criticality   |         |    cve_id              |
|    risk_score    |         |    status (OPEN/FIXED) |
|    fin_exposure  |         |    discovered_date     |
+------------------+         |    remediation_deadline|
                             +------------------------+
```

#### 4. `assets` (Digital Inventory)
Represents servers, databases, cloud clusters, firewalls, and endpoints.
* `criticality VARCHAR(32)`: Business weight (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
* `risk_score NUMERIC(6, 2)`: Evaluated score between `0.00` and `100.00`.
* `financial_exposure NUMERIC(19, 2)`: The total monetary exposure in Indian Rupees or USD if this asset suffers a catastrophic breach.

#### 5. `vulnerabilities` (Weaknesses & Flaws)
Represents specific unpatched bugs or misconfigurations on an asset.
* `asset_id BIGINT REFERENCES assets(id)`: Links the flaw directly to the impacted hardware/software.
* `cve_id VARCHAR(40)`: Standard NIST Common Vulnerabilities and Exposures identifier (e.g., `CVE-2024-21413`).
* `severity VARCHAR(32)`: Standard CVSS scale (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
* `remediation_deadline DATE`: SLA deadline for the security operations team to patch the bug.

---

### Cluster 3: Threat Intelligence & Junction Mapping

```
+--------------------+       +------------------------+       +------------------+
|    THREAT_INTEL    | 1   * |  THREAT_INTEL_ASSETS   | *   1 |      ASSETS      |
|--------------------|<-----+|------------------------|+----->|------------------|
| PK id              |       | PK,FK threat_id        |       | PK id            |
| FK org_id          |       | PK,FK asset_id         |       |    name          |
|    title           |       +------------------------+       +------------------+
|    threat_actor    |       
|    severity        |       
|    published_date  |       
+--------------------+       
```

#### 6. `threat_intel` (External Threat Feeds)
Stores curated intelligence on ransomware gangs, zero-day exploits, and phishing campaigns (e.g., *"LockBit 3.0 Campaign"*, *"Volt Typhoon APT"*).

#### 7. `threat_intel_assets` (Many-to-Many Junction Table)
> 🎓 **Professor's Note: What is a Junction Table?**  
> One threat (like LockBit ransomware) can threaten dozens of servers. And one server can be threatened by dozens of different hacker gangs. This is a **Many-to-Many (N:M)** relationship. Relational databases cannot store lists directly in a column cleanly. Instead, we create a bridge table with a composite primary key `(threat_id, asset_id)` that links pairs together.

---

### Cluster 4: The Optimization & Simulation Sandbox

This cluster stores the mathematical engine data: security controls, investments, and what-if scenarios.

```
+-----------------------+ 1     * +-----------------------+
|  REMEDIATION_ACTIONS  |--------<|  APPLIED_INVESTMENTS  |
|-----------------------|         |-----------------------|
| PK id                 |         | PK id                 |
| FK org_id             |         | FK org_id             |
|    name               |         | FK action_id          |
|    cost               |         |    cost               |
|    risk_reduct_pct    |         |    applied_at         |
|    category           |         +-----------------------+
|    active             |
+-----------------------+
           | 1
           |
           | *
+-----------------------+
|   WHATIF_SCENARIOS    |
|-----------------------|
| PK id                 |
| FK org_id             |
|    name               |
| FK control_id         |
|    delay_days         |
+-----------------------+
```

#### 8. `remediation_actions` (Security Controls Catalog)
Available defensive countermeasures that a CISO can buy (e.g., *"Hardware MFA Rollout"*, *"EDR Next-Gen Agent"*, *"Database Transparent Encryption"*).
* `cost NUMERIC(19, 2)`: The implementation price.
* `risk_reduction_percent NUMERIC(6, 2)`: The mathematically quantified percentage by which this control reduces organizational risk.

#### 9. `applied_investments` (Committed Capital)
Whenever an executive clicks **"Apply Optimal Plan"** on the dashboard, this table records the timestamped purchase and adjusts the organization's remaining budget.

#### 10. `whatif_scenarios` (Simulation Models)
Stores "what if" sandbox hypotheses: *"What happens if we delay deploying Endpoint Protection by 60 days?"*

---

### Cluster 5: Historical Trends, Alerts & Audits

#### 11. `risk_snapshots` (Time-Series Metric Ledger)
* `organization_id` + `snapshot_date`: Enforced by a unique constraint `uk_risk_snap UNIQUE (organization_id, snapshot_date)`.
* Stores the daily overall score and monetary exposure so the frontend can render silky-smooth 30-day and 90-day SVG trend graphs.

#### 12. `alerts` (Real-Time Security Notifications)
* `asset_id` & `vulnerability_id`: Optional foreign keys that directly connect an alert banner to the offending asset or CVE.
* `read BOOLEAN NOT NULL DEFAULT FALSE`: Tracks unread notification badges in the UI bell icon.

---

### Cluster 6: Monetization & SaaS Billing

```
+------------------+ 1     * +-----------------------------+ *     1 +------------------+
|  ORGANIZATIONS   |--------<|  ORGANIZATION_SUBSCRIPTIONS |>--------|  BILLING_PLANS   |
+------------------+         +-----------------------------+         +------------------+
         | 1                                                                   |
         |                                                           (STARTER, GROWTH,  |
         +---+---------------------+                                  ENTERPRISE tiers) |
             | 1                   | 1                                                 |
             v *                   v *                                                 |
     [PAYMENT_METHODS]         [INVOICES]                                              |
```

#### 13. `billing_plans`
Stores subscription tiers:
* `STARTER`: ₹99,000 / mo (Up to 50 assets, basic alerts).
* `GROWTH`: ₹2,49,000 / mo (Up to 500 assets, Knapsack optimizer, What-if analysis).
* `ENTERPRISE`: ₹7,99,000 / mo (Unlimited assets, custom reports, SLA support).

#### 14. `organization_subscriptions`, `payment_methods`, & `invoices`
Manages card brand/last4 tokens (via Razorpay/Stripe mock gateways), renewal dates, and downloadable PDF invoice records.

---

## 6. Multi-Tenancy: How We Guarantee Zero Data Leaks

One of the most critical questions judges and security auditors ask:  
**"How do you make sure University A cannot view Bank B's data in a shared database?"**

CyberShield implements **Discriminator Column Multi-Tenancy with Spring AOP Defense in Depth**:

```
+-----------------------------------------------------------------------------------------+
|                                    INCOMING REQUEST                                     |
|               GET /api/assets/42  [Bearer JWT: orgId=1, role=ANALYST]                   |
+-----------------------------------------------------------------------------------------+
                                             |
                                             v
                           [1. Spring Security Filter]
                           Extracts orgId=1 from cryptographically signed JWT.
                                             |
                                             v
                           [2. OrgAccessAspect (@AssertOrgAccess)]
                           Intercepts method execution BEFORE database query!
                           Checks: "Does Asset #42 belong to orgId=1?"
                                             |
                     +-----------------------+-----------------------+
                     | MATCH                                         | MISMATCH
                     v                                               v
       [3. Spring Data JPA Query]                    [HTTP 403 FORBIDDEN]
       SELECT * FROM assets                          Execution BLOCKED immediately!
       WHERE id = 42 AND organization_id = 1         Logged as potential breach attempt.
```

1. **Every SQL query filters by `organization_id`**: No query is ever executed without an explicit tenant predicate.
2. **Database-level Foreign Keys**: No orphan records can be created under an unassigned tenant.
3. **Automated Unit & Integration Tests**: Verify that attempting cross-tenant access produces an immediate `403 Access Denied`.

---

## 7. Performance Engineering: Indexes & Query Optimization

Without database indexes, querying an asset from an organization with 1,000,000 records requires the database to perform a **Full Table Scan ($O(N)$)**—inspecting every single row from disk.

CyberShield adds strategic **B-Tree Indexes**:

| Index Name | Table | Columns | Purpose | Time Complexity |
| :--- | :--- | :--- | :--- | :--- |
| `idx_users_org` | `users` | `(organization_id)` | Fast lookup of team members belonging to a company | $O(\log N)$ |
| `idx_assets_org` | `assets` | `(organization_id)` | Instant loading of the organization's asset inventory | $O(\log N)$ |
| `idx_vulns_org` | `vulnerabilities` | `(organization_id)` | High-speed filtering of unresolved CVEs | $O(\log N)$ |
| `idx_alerts_org` | `alerts` | `(organization_id)` | Rapid loading of top notification bell alerts | $O(\log N)$ |
| `uk_risk_snap` | `risk_snapshots` | `(organization_id, snapshot_date)` | Unique composite index for rapid 30-day timeline charts | $O(\log N)$ |

---

## 8. Schema Evolution & Versioning: Flyway Migrations

In amateur projects, developers manually alter database tables using visual tools like pgAdmin. But what happens when you deploy to Render, AWS, or production? Someone forgets a column, and the server crashes!

CyberShield uses **Flyway Database Migrations**:
* Every schema change is written as an immutable SQL migration file inside `backend/src/main/resources/db/migration/`.
* When the Spring Boot backend starts up on Render or locally, Flyway inspects the database:
  1. Checks table `flyway_schema_history`.
  2. Applies pending migrations in strict numerical order (`V1__init.sql` ➔ `V2__billing_plans.sql`).
  3. Updates schema version atomically inside a database transaction.

```
Local Dev (H2 In-Memory)  <-- Both run identical Flyway scripts -->  Production (Render PostgreSQL 16)
```
This guarantees that **development, testing, and production environments are ALWAYS 100% synchronized**.

---

## 9. Review Questions (Judge & Viva Preparation) 💡

Here are the questions judges love to ask regarding database design, and how to answer them like a seasoned engineer:

### Q1: "Why did you choose a Relational Database (PostgreSQL) instead of NoSQL (MongoDB)?"
> **Winning Answer:**  
> *"CyberShield deals with financial risk quantification, regulatory compliance deadlines, and strict multi-tenant access control. These require strict ACID guarantees (Atomicity, Consistency, Isolation, Durability) and complex relational joins between Assets, CVEs, Threats, and Budgets. In a NoSQL document database, maintaining referential integrity when deleting an asset or computing Knapsack optimization would require error-prone application-level cascades and duplicate data."*

### Q2: "How do you protect passwords and sensitive authentication tokens?"
> **Winning Answer:**  
> *"We never store plaintext credentials. Passwords are encrypted using the salted BCrypt one-way hashing function with a cost factor of 10. Refresh tokens and password reset tokens are digested using SHA-256 before insertion into `refresh_tokens` and `password_reset_tokens`. Even in the event of a physical database dump leakage, the attacker cannot reverse the hashes into active tokens."*

### Q3: "What prevents floating-point rounding errors in your financial risk calculations?"
> **Winning Answer:**  
> *"All financial figures—such as asset financial exposure, total risk reduction values, and security control costs—use `NUMERIC(19, 2)` mapped to Java's `BigDecimal`. This completely eliminates binary floating-point representation anomalies inherent in standard IEEE 754 float/double types."*

---

## 10. Quick Knowledge Check 📝

### Question 1
Why are refresh tokens hashed using SHA-256 before being stored in the `refresh_tokens` table?
- [ ] A) To make the database file smaller on disk
- [x] B) So that an attacker reading a leaked database cannot impersonate users with active session tokens
- [ ] C) Because PostgreSQL doesn't support strings longer than 64 characters
- [ ] D) To convert the token into an integer

*Explanation: Storing token hashes ensures that even with read access to the database, an intruder cannot forge or present the original bearer token to the server.*

---

### Question 2
What SQL constraint prevents a company from having two different risk trend records on the exact same calendar date?
- [ ] A) `CHECK (snapshot_date > '2020-01-01')`
- [ ] B) `FOREIGN KEY (organization_id)`
- [x] C) `CONSTRAINT uk_risk_snap UNIQUE (organization_id, snapshot_date)`
- [ ] D) `PRIMARY KEY (snapshot_date)`

*Explanation: The composite unique constraint `(organization_id, snapshot_date)` ensures that there is at most one snapshot per organization per day.*

---

### Question 3
What happens to a user's active refresh tokens if their user row is deleted from the `users` table?
- [ ] A) The tokens remain in the table forever as orphan records
- [ ] B) The database throws a Foreign Key Violation error and aborts
- [x] C) The tokens are automatically deleted because of `ON DELETE CASCADE`
- [ ] D) The tokens are emailed to the administrator

*Explanation: `REFERENCES users(id) ON DELETE CASCADE` tells the database engine to clean up all child records associated with the deleted parent automatically.*

---

## 11. What Did We Learn Today? 🌟

1. **Relational Rigor**: CyberShield uses **PostgreSQL 16** with 20 normalized tables to maintain flawless referential integrity.
2. **Exact Financial Math**: We use `NUMERIC(19, 2)` (mapped to Java's `BigDecimal`) to ensure zero rounding errors in multi-million rupee cyber exposure calculations.
3. **Multi-Tenant Protection**: Every tenant table is keyed by `organization_id`, reinforced by Spring AOP aspect security guards.
4. **Zero Plaintext Credentials**: Passwords use BCrypt; refresh tokens and reset tokens use SHA-256 digests.
5. **Repeatable Migrations**: Flyway version-controls our schema across development, staging, and production on Render.

---

*Now that our database vault is sealed and fully understood, are you ready to explore the mathematical heart of the platform? In **Chapter 7: Business Logic & Algorithms**, we will dissect the **Monte Carlo Risk Formulation** and the **0/1 Knapsack Budget Optimizer**!*
