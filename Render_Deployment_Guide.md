# 🚀 CyberShield AI — Render Deployment Guide

> **Deploying CyberShield AI to Render without Docker**  
> *Production Stack: Render Managed PostgreSQL + Spring Boot Web Service + React Static Site*

---

## 🏗️ 1. Architecture Overview on Render

When deployed to Render, CyberShield runs across three dedicated cloud resources:

```
+─────────────────────────────────────────────────────────────────────────────+
|                         CYBERSHIELD ON RENDER                               |
+─────────────────────────────────────────────────────────────────────────────+
|                                                                             |
|   1. FRONTEND STATIC SITE            2. BACKEND WEB SERVICE                 |
|      (cybershield-web.onrender.com)     (cybershield-api.onrender.com)      |
|      • Type: Static Site                • Type: Web Service                 |
|      • Runtime: React 18 + Vite 5       • Runtime: Spring Boot 3.3.4 (Java 21)
|      • Global CDN, HTTPS by default     • Auto-detects Render PORT (10000)  |
|      • Single Page App (SPA) rewrite    • Flyway migrations V1 & V2 auto-run|
|                                                     │                       |
|                                                     │ Secure JDBC Connection|
|                                                     ▼                       |
|                                      3. MANAGED DATABASE                    |
|                                         (Render PostgreSQL 16)              |
|                                         • Internal & External URLs          |
|                                         • High-performance relational DB    |
+─────────────────────────────────────────────────────────────────────────────+
```

---

## ⚡ Option A: Fast-Track Deployment (Using `render.yaml` Blueprint)

The repository includes a pre-configured `render.yaml` Blueprint specification. This allows you to deploy the **Database**, **Backend**, and **Frontend** in one click.

### Steps:
1. **Push your code to GitHub** (make sure your repo is private or public on your GitHub account).
2. Log in to [dashboard.render.com](https://dashboard.render.com).
3. Click **New +** in the top navigation bar and select **Blueprint**.
4. Connect your GitHub repository.
5. Render will automatically read `render.yaml` and display:
   * **Database**: `cybershield-db` (PostgreSQL Free Tier)
   * **Web Service**: `cybershield-api` (Spring Boot Backend)
   * **Static Site**: `cybershield-web` (React Frontend)
6. Click **Apply**.
7. Render will provision the database, build and launch the backend, build the frontend, and automatically link the connection strings!

---

## 🛠️ Option B: Manual Step-by-Step Deployment (Via Render Dashboard)

If you prefer to configure each service manually in the Render UI, follow these steps:

---

### Step 1: Create the Render PostgreSQL Database

1. In Render Dashboard, click **New +** -> **PostgreSQL**.
2. Configure the database:
   * **Name**: `cybershield-db`
   * **Database**: `cybershield`
   * **User**: `cybershield`
   * **Region**: `Oregon (US West)` *(or your preferred region)*
   * **Plan**: `Free`
3. Click **Create Database**.
4. Once created, keep the page open. You will need:
   * **Internal Database URL** (for backend services in the same Render region):  
     `postgresql://cybershield:PASSWORD@dpg-xxxx-a:5432/cybershield`

---

### Step 2: Create the Spring Boot Backend Web Service

1. In Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Configure the service:
   * **Name**: `cybershield-api`
   * **Region**: Same region as your database (e.g., `Oregon`)
   * **Branch**: `main` *(or your working branch)*
   * **Root Directory**: `backend`
   * **Runtime**: `Node` *(Render's Debian Linux environment; our script automatically provisions OpenJDK 21)*
   * **Build Command**: `./render-build.sh`
   * **Start Command**: `./render-start.sh`
   * **Instance Type**: `Free`

4. Scroll down to **Environment Variables** and add:

| Key | Value | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates PostgreSQL & production settings |
| `DATABASE_URL` | *(Paste Internal Database URL from Step 1)* | Auto-translated to JDBC format by `render-start.sh` |
| `JWT_SECRET` | `generate-a-random-32-char-secret-string-here` | Sign your session tokens securely |
| `CORS_ALLOWED_ORIGINS` | `https://*.onrender.com,http://localhost:5173` | Allows all Render frontend subdomains |
| `APP_SEED` | `true` | Auto-seeds demo data (ABC University) on first start |

5. Under **Health Check Path**, enter: `/actuator/health`
6. Click **Create Web Service**.

> Render will build the backend using `render-build.sh`, run Flyway migrations against PostgreSQL, seed the demo data, and bind to Render's dynamic `$PORT`.  
> Once live, copy your backend URL: e.g., `https://cybershield-api.onrender.com`.

---

### Step 3: Create the React Frontend Static Site

1. In Render Dashboard, click **New +** -> **Static Site**.
2. Connect your GitHub repository.
3. Configure the static site:
   * **Name**: `cybershield-web`
   * **Branch**: `main` *(or your working branch)*
   * **Root Directory**: `frontend`
   * **Build Command**: `npm install && npm run build`
   * **Publish Directory**: `dist`

4. Under **Environment Variables**, add:

| Key | Value | Notes |
|---|---|---|
| `VITE_API_BASE_URL` | `https://cybershield-api.onrender.com` | Your backend URL from Step 2 (without trailing slash) |

5. Under **Redirects / Rewrites**, click **Add Rule**:
   * **Type**: `Rewrite`
   * **Source**: `/*`
   * **Destination**: `/index.html`  
   *(This ensures React Router client-side routing works on page refreshes like `/app/overview`).*

6. Click **Create Static Site**.

---

## 📋 Environment Variables Master Reference

### Backend (`cybershield-api`):
```properties
# Active profile (uses application-prod.yml)
SPRING_PROFILES_ACTIVE=prod

# Render PostgreSQL URL (auto-translated to jdbc:postgresql:// by render-start.sh)
DATABASE_URL=postgresql://cybershield:password@dpg-xxx-a:5432/cybershield

# JWT Signing Secret (must be at least 32 characters)
JWT_SECRET=cybershield-ai-production-super-secret-key-32-chars

# CORS Origin Allow-list
CORS_ALLOWED_ORIGINS=https://cybershield-web.onrender.com,https://*.onrender.com

# Seed Demo Data on Startup (set to false for a blank enterprise instance)
APP_SEED=true
```

### Frontend (`cybershield-web`):
```properties
# Backend API Base URL
VITE_API_BASE_URL=https://cybershield-api.onrender.com
```

---

## ✅ Post-Deployment Verification Checklist

Once your services show **Live** in green on the Render Dashboard:

- [ ] **Backend Health Check**:  
  Open `https://cybershield-api.onrender.com/actuator/health` in your browser.  
  *Expected response*: `{"status":"UP"}`.

- [ ] **Swagger Documentation**:  
  Open `https://cybershield-api.onrender.com/swagger-ui.html`.  
  *Expected*: Interactive OpenAPI documentation page listing all endpoints.

- [ ] **Frontend Web App**:  
  Open `https://cybershield-web.onrender.com`.  
  *Expected*: Clean CyberShield login screen.

- [ ] **Demo Sign-In**:  
  Sign in with seeded demo credentials:
  * **Email**: `admin@abc.university`
  * **Password**: `Admin#2026Ai`  
  *Expected*: Lands on `/app/overview` with live metrics (Score: `65.7 / 100`, Exposure: `₹ 5.19 Cr`).

- [ ] **F5 Page Refresh**:  
  Press **F5** while inside `/app/overview`.  
  *Expected*: The page reloads seamlessly without logging you out (session verified via `localStorage` token).

- [ ] **New Organization Registration**:  
  Sign out, click **Register**, and create a new account:
  * **Organization**: `Apollo Tech`
  * **Full Name**: `Dr. Alan Turing`
  * **Email**: `alan@apollotech.com`
  * **Password**: `SecurePass#2026`  
  *Expected*: Instant account creation, growth subscription provisioned, and direct redirect to dashboard.

---

## ❓ Troubleshooting & FAQs

### 1. The backend says "Application failed to start" / Database connection refused
* **Cause**: Incorrect database URL or database is not yet ready.
* **Fix**: Verify that `DATABASE_URL` contains Render's **Internal Database URL** (e.g. `postgresql://cybershield:...@dpg-xxx-a:5432/cybershield`), not localhost.

### 2. Frontend says "Login failed" / Network Error
* **Cause**: `VITE_API_BASE_URL` is missing, misconfigured, or backend is sleeping.
* **Fix**:
  1. Check that `VITE_API_BASE_URL` in the frontend static site settings matches your backend URL (e.g., `https://cybershield-api.onrender.com`).
  2. Because Render Free Tier web services spin down after 15 minutes of inactivity, the first API request may take ~30-50 seconds to wake the server up. Subsequent calls respond in milliseconds.

### 3. Page refresh shows "Not Found" on the frontend
* **Cause**: Missing SPA rewrite rule.
* **Fix**: In the frontend static site settings on Render, go to **Redirects/Rewrites** and add:
  * Type: `Rewrite`
  * Source: `/*`
  * Destination: `/index.html`

### 4. Are Docker files still available?
* **Yes!** All original Docker configuration files (`Dockerfile`, `docker-compose.yml`, `backend/Dockerfile`) remain untouched in the repository for future containerized or cloud deployments (AWS ECS, GCP Cloud Run, or Kubernetes).
