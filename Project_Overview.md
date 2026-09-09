# CyberShield AI — Project Overview

## 1. Introduction

CyberShield AI is a web-based cyber risk management and financial quantification platform. It helps organizations—such as universities, hospitals, banks, and businesses—understand their computer security risks in simple business terms.

Instead of only showing technical computer errors and confusing security codes, CyberShield AI translates technical weaknesses into two clear metrics:
1. **A simple risk score from 0 to 100** that indicates how safe or unsafe the organization is.
2. **An exact financial amount (in currency, such as Indian Rupees ₹)** showing how much money the organization could lose if a cyberattack occurs.

By connecting technical security data with financial impact, CyberShield AI enables management, executives, and IT teams to make informed decisions about where to invest their security budget for maximum protection.

---

## 2. The Problem It Solves

Modern organizations face three major challenges when managing cybersecurity:

1. **Communication Gap Between IT Teams and Management**: Technical security tools generate thousands of complex alert codes (like CVE numbers, open ports, and threat logs). IT administrators understand these terms, but directors, managers, and board members do not. This makes it difficult for leadership to understand the actual danger.
2. **Lack of Financial Clarity**: Most security tools tell a company that a vulnerability is "High" or "Critical", but they cannot answer the most important business question: *"How much money will we lose if this system gets hacked?"*
3. **Budget Waste and Confusion**: Organizations have limited money to spend on cybersecurity. When IT teams ask for security upgrades, management does not know which projects to approve first. Without clear data, companies often waste money on low-priority tools while leaving critical systems unprotected.

CyberShield AI solves these problems by providing a unified platform that automatically calculates the financial cost of cyber risks and recommends the most cost-effective security actions within a given budget.

---

## 3. Project Objectives

The main goals of CyberShield AI are:

* **Translate Technical Data into Business Insights**: Convert complex vulnerability logs into easy-to-read risk scores and monetary figures.
* **Provide Continuous Asset Visibility**: Maintain a central inventory of all computers, servers, websites, databases, and network devices owned by the organization.
* **Quantify Cyber Risk in Real Currency**: Estimate the realistic financial exposure (potential loss from downtime, data loss, and recovery costs) for every digital asset.
* **Optimize Security Spending**: Use mathematical algorithms to tell decision-makers exactly which security controls to buy to get the highest risk reduction for their budget.
* **Simulate "What-If" Scenarios**: Allow managers to see what happens to their risk level and financial exposure if they delay a security project by 30, 60, or 90 days.
* **Enable Secure Multi-Tenant Access**: Allow different organizations to use the platform independently while guaranteeing that their data remains completely private and isolated.

---

## 4. How the Application Works

CyberShield AI follows a simple, continuous step-by-step process:

1. **Asset Inventory**: The organization registers its digital assets (servers, portals, payment gateways, databases, and laptops) into the system.
2. **Vulnerability Mapping**: The system records security weaknesses and software flaws (CVEs) found on each asset, along with their severity level.
3. **Threat Intelligence Correlation**: External threat intelligence (such as active ransomware campaigns, hacker groups, and emerging exploits) is linked to the relevant assets.
4. **Risk and Financial Calculation**: The backend engine evaluates the importance of the asset, the severity of the weakness, and the likelihood of attack. It calculates:
   * An overall risk score (0 to 100).
   * A total financial exposure amount (in ₹).
5. **Budget Optimization**: When an organization specifies its available security budget, the system identifies the best combination of security actions to reduce the most risk for that money.
6. **Executive Reporting & Alerts**: The platform sends immediate alerts for high-risk issues and generates one-click summary reports for leaders and auditors.

---

## 5. Main Features of the Application

* **Executive Dashboard**: A single screen showing overall risk, money at risk, critical systems, and a 30-day security trend.
* **Asset Management**: A searchable list of all physical and digital computers, showing their type, owner, status, risk score, and financial exposure.
* **Vulnerability Management**: A tracking table for all software bugs and misconfigurations, including deadlines for fixing them and status tracking.
* **Threat Intelligence**: A live feed of active cyber threats with details on attack methods, severity, and which organizational assets are targeted.
* **Investment Optimizer**: A decision-support tool that calculates the best security investments for any chosen budget.
* **What-If Simulation Sandbox**: A testing tool that predicts the impact on risk and finances if security upgrades are delayed.
* **Alerts and Notifications**: An in-app alert center that notifies users when new critical threats or overdue vulnerabilities appear.
* **Automated Executive Reports**: Generation of clean summary reports that can be downloaded as PDF or CSV files for board meetings and audits.
* **Role-Based Access Control**: Three built-in user roles (Admin, Analyst, and Viewer) to control who can view, edit, or manage settings.

---

## 6. Explanation About the Dashboard

The Dashboard is the central home screen of CyberShield AI. It provides an immediate high-level summary designed for both non-technical leaders and security analysts:

* **Overall Risk Score (0–100)**: A single health score. A low number (e.g., 0–30) means safe; a high number (e.g., 60–100) means urgent action is required.
* **Financial Exposure**: The total estimated money (in ₹) that the organization could lose across all its systems if an attack succeeds.
* **Critical Assets at Risk**: The count of high-priority systems that currently have unpatched severe vulnerabilities.
* **Top Threat Level**: The highest danger level among all currently active threats (Low, Medium, High, or Critical).
* **30-Day Trend Curve**: A graphical timeline showing whether the organization's risk is increasing, decreasing, or remaining steady over time.
* **Risk Category Breakdown**: A visual chart dividing risk across different areas, such as Web Applications, Databases, Internal Networks, and Cloud Services.
* **Plain-English Summary**: A clear summary explaining the current security situation in simple language and highlighting the most important next step.

---

## 7. Explanation on Vulnerability Management

Vulnerabilities are software bugs, outdated versions, or configuration mistakes that attackers can use to break into a computer system.

CyberShield AI manages vulnerabilities through a dedicated module:
* **Identification**: Every vulnerability is recorded with its standard industry identifier (CVE ID, such as `CVE-2024-21413`) and a descriptive title.
* **Asset Association**: Each vulnerability is directly linked to the specific asset where it was detected (for example, *Student Exam Portal* or *Main Database*).
* **Severity Rating**: Weaknesses are classified using four clear levels: **Critical**, **High**, **Medium**, and **Low**.
* **Remediation Deadlines**: A calendar deadline is assigned to each vulnerability so IT teams know how much time they have to fix it before policy SLAs are violated.
* **Resolution Tracking**: Users can track the status of each flaw (**Open** or **Fixed**) and apply patches with a single click in the interface to immediately update the organization's risk score.

---

## 8. Threat Detection and Intelligence

Threat Intelligence focuses on what attackers are doing on the public internet.

* **Tracking Attackers and Malware**: The system tracks active ransomware groups, automated scanning tools, and known hacker campaigns.
* **Direct Asset Linkage**: Instead of showing generic global news, CyberShield AI maps each threat directly to the company's internal assets. If a ransomware campaign targets a specific database brand, the system highlights only the databases owned by the organization that run that brand.
* **Severity Levels**: Threats are prioritized by their actual danger level so teams can focus on what matters most today.

---

## 9. Risk Assessment and Quantification

CyberShield AI calculates risk using a practical mathematical method rather than guesswork:

$$\text{Risk} = \text{Threat Likelihood} \times \text{Vulnerability Severity} \times \text{Asset Value}$$

1. **Asset Value**: How vital the system is to daily operations (e.g., a core payment system is worth far more than a public blog).
2. **Vulnerability Severity**: How easy the bug is to exploit and how much control it gives an attacker.
3. **Threat Likelihood**: Whether attackers are actively using this exploit in the wild.

The result is converted into:
* A normalized score between **0.00 and 100.00**.
* An exact monetary **Financial Exposure** value in Indian Rupees, representing potential revenue loss, recovery costs, and downtime impact.

---

## 10. Investment Optimization and What-If Analysis

Most organizations do not have enough money to fix every security weakness at once. CyberShield AI solves this with two intelligent tools:

### Investment Optimizer (Knapsack Algorithm)
* The user enters their available budget (for example, ₹5,00,000).
* The system evaluates a catalog of available security controls (such as Multi-Factor Authentication, Endpoint Detection, or Network Firewalls), each having a specific cost and a proven risk-reduction percentage.
* Using mathematical optimization (the 0/1 Knapsack method), the system selects the exact combination of controls that delivers the **maximum possible risk reduction** without exceeding the budget.
* An executive can review the recommendation and click **"Apply Investment"** to update the organization's risk model.

### What-If Simulation
* Management can simulate future decisions before committing money.
* For example: *"What happens if we postpone purchasing Endpoint Protection for 60 days?"*
* The system calculates and displays the projected rise in risk score and the increased financial exposure over that delay period.

---

## 11. Reports and Analytics

CyberShield AI provides reporting tools to keep leadership, board members, and external auditors informed:

* **Executive Summary**: A concise high-level report highlighting the current risk score, total financial exposure, and key accomplishments.
* **Detailed Inventory Reports**: Full technical exports of all assets, active vulnerabilities, and threat feeds.
* **Export Formats**: Reports can be downloaded instantly in standard formats (such as PDF and CSV) for offline review or compliance documentation.
* **Audit History**: A saved record of all previously generated reports, including creation dates and author details.

---

## 12. Application Architecture and Data Flow

The application is built using a clean, modern three-tier architecture:

```
[ User's Browser (React SPA) ]
              |
              | HTTPS (Secure REST API calls with JSON & JWT)
              v
[ Backend Web Service (Spring Boot 3.3.4 / Java 21) ]
  • Authentication Filter (verifies user identity)
  • Security & Tenant Guard (prevents cross-company data access)
  • Business Logic Services (risk math & budget optimizer)
              |
              | JDBC Connection
              v
[ Database Engine (PostgreSQL 16) ]
  • 20 Normalized Relational Tables
  • Automated Flyway Migrations
```

### Data Flow Example:
1. The user logs into the React frontend using their email and password.
2. The Spring Boot backend validates the credentials, generates an encrypted access token (JWT), and returns it to the browser.
3. When the user opens the Dashboard, the frontend requests `/api/risk/overview` with the token.
4. The backend verifies the token, queries only the records that belong to that user's organization from PostgreSQL, computes the risk score and financial exposure, and returns the data as JSON.
5. The browser renders the interactive dashboard, charts, and summary cards.

---

## 13. Technologies Used

CyberShield AI is built using reliable, modern, industry-standard technologies:

* **Frontend**:
  * **React 18**: Modern component-based user interface library.
  * **Vite 5**: High-speed frontend build tool and development server.
  * **React Router**: Client-side page navigation without page reloads.
  * **Axios & TanStack React Query**: Efficient data fetching, caching, and state synchronization.
* **Backend**:
  * **Java 21**: High-performance, modern Long-Term Support (LTS) Java runtime.
  * **Spring Boot 3.3.4**: Enterprise-grade framework for building secure REST APIs.
  * **Spring Security 6**: Authentication and role-based authorization filter chain.
  * **Spring Data JPA & Hibernate**: Object-relational mapping to interact with the database.
* **Database**:
  * **PostgreSQL 16**: Industrial-strength relational database used in production.
  * **H2 Database**: Fast in-memory database used for local unit testing and quick demos.
  * **Flyway**: Automated database migration tool that tracks and applies schema updates version by version.
* **Cloud & Deployment**:
  * **Render Cloud**: Fully automated cloud hosting with a React Static Site frontend, Spring Boot Web Service backend, and Managed PostgreSQL database.

---

## 14. Security Features

Security is built directly into every layer of CyberShield AI:

* **Encrypted Passwords**: Passwords are never stored in plain text. They are hashed using the industry-standard **BCrypt** algorithm.
* **Dual-Token Authentication**: Secure access using short-lived **JSON Web Tokens (JWT)** for API requests and encrypted **Refresh Tokens** for seamless background session renewal.
* **Multi-Tenant Data Isolation**: Every piece of data is tagged with an `organization_id`. The backend enforces checks on every query so that Company A can never view or modify Company B's information.
* **Cross-Origin Protection (CORS)**: Strict rules restrict API requests so that only verified domains (such as the official frontend on Render) can communicate with the backend.
* **Role-Based Permissions**:
  * **Admin**: Full control over users, assets, budgets, and settings.
  * **Analyst**: Can create assets, update vulnerabilities, and run simulations.
  * **Viewer**: Read-only access for executives and board members who only need to review metrics and reports.

---

## 15. Why CyberShield AI is Useful (Key Benefits)

* **Speaks the Language of Business**: Turns scary computer jargon into simple scores and exact currency figures that anyone can understand.
* **Saves Money**: Prevents wasted security spending by mathematically identifying the most effective defensive actions for a specific budget.
* **Faster Decision-Making**: Executives can review clear dashboards and approve security budgets in minutes instead of wading through technical spreadsheets.
* **Proactive Defense**: By highlighting active threats before they strike, teams can fix the most critical weaknesses first.
* **Easy to Deploy and Maintain**: Runs as a lightweight web application in the cloud with zero software installation required for end users.
