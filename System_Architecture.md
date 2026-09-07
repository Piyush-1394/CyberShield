# 🏛️ Chapter 2: System Architecture — How the Machine is Built

> *"Architecture is not just how something looks on the outside. It is the blueprint of how every single moving part connects, communicates, and protects the whole building."*  
> — Welcome back! Today, we open the hood of CyberShield AI and look inside.

---

## 1. What Are We Talking About?

**System Architecture is the master blueprint that explains what parts make up the CyberShield software, where each part lives, and how they talk to each other to keep your data safe.**

---

## 2. Why Do We Need an Architecture?

Imagine trying to build a 50-story hospital without a blueprint:
* The plumbers might put water pipes right through the electrical wires!
* The security guards might not know which doors lead to the medicine vault!
* If the power goes out, the whole building might collapse into chaos!

Software is just like a modern building. If you don't design it with clean, separate rooms:
1. A small mistake in the login screen could crash the entire database.
2. Hackers could sneak directly into the private database without anyone checking their ID.
3. The system would become slow, messy, and impossible to fix when something breaks.

A clean **System Architecture** ensures that every part of CyberShield has **one clear job**, sits in its **own safe room**, and communicates through **well-guarded checkpoints**.

---

## 3. Imagine This: The Grand 5-Star Hotel 🏨

To understand how CyberShield is organized, imagine visiting a luxurious 5-star hotel called **The Cyber Grand Hotel**.

```
+-------------------------------------------------------------------------------+
|                         THE CYBER GRAND HOTEL                                 |
+-------------------------------------------------------------------------------+
|                                                                               |
|   1. GUEST RECEPTION         2. SECURITY CHECKPOINT     3. MASTER KITCHEN     |
|      (Frontend - React)         (Security Filter)          (Backend Services) |
|      • Beautiful lobby          • Scans luggage            • Master Chefs     |
|      • Touchscreen menu         • Checks guest passes      • Calculates math  |
|      • Smiles & greets you      • Stops intruders          • Prepares meals   |
|                                                                    ||         |
|                                                                    ||         |
|                                                         4. THE SECURE VAULT   |
|                                                            (Database - H2/PG) |
|                                                            • Underground safe |
|                                                            • Heavy steel doors|
|                                                            • Stores records   |
+-------------------------------------------------------------------------------+
```

Notice how this hotel operates:

1. **The Reception Desk (The Frontend)**:
   When you walk in, you see marble floors, polite staff, and an iPad displaying the menu. You can look at pictures, tap buttons, and pick what you want. But there is NO food cooked here, and NO money stored here. It is purely designed for you to interact comfortably.

2. **The Security Guard at the Door (The Security Filter)**:
   Before you can enter the private dining areas, a security guard checks your wristband. If you don't have a valid hotel wristband, you are politely shown the exit door. You cannot sneak into the kitchen.

3. **The Kitchen Chefs (The Backend Business Logic)**:
   Behind the heavy double doors is the bustling kitchen. The chefs don't talk to the guests directly; they receive printed order slips from the waiters. The chefs know the secret recipes, weigh the ingredients, calculate portions, and prepare the food.

4. **The Underground Cold Storage & Vault (The Database)**:
   Behind the kitchen is a temperature-controlled vault. Only the head chefs have keys. This is where fresh vegetables, spices, accounting ledgers, and cash reserves are locked up safely.

**CyberShield AI is designed exactly like The Cyber Grand Hotel!**

---

## 4. Now Connect That Story to CyberShield

Let's translate our hotel into software components:

```
+-------------------------------------------------------------------------------+
|                        CYBERSHIELD HIGH-LEVEL BLUEPRINT                       |
+-------------------------------------------------------------------------------+
|                                                                               |
|   [ USER'S BROWSER ]                                                          |
|   (Laptop, Tablet, or Phone)                                                  |
|           │                                                                   |
|           │ 1. User clicks or types in browser                                |
|           ▼                                                                   |
|   [ FRONTEND: React 18 SPA ]  (Port 5173)                                     |
|   • Draws the dashboards, charts, and buttons                                 |
|   • Stores login token safely in localStorage                                 |
|   • Runs the Vite Reverse Proxy to forward requests                           |
|           │                                                                   |
|           │ 2. Sends encrypted HTTP message with JWT Token                    |
|           ▼                                                                   |
|   [ SECURITY GUARD: Spring Security & JwtFilter ]                             |
|   • Inspects the digital token (wristband)                                    |
|   • Rejects invalid or expired visitors with "401 Unauthorized"               |
|   • Reads the user's role (Admin, Analyst, or Viewer)                         |
|           │                                                                   |
|           │ 3. Hands request to Controller                                    |
|           ▼                                                                   |
|   [ BACKEND BRAIN: Spring Boot 3.3.4 ]  (Port 8080)                           |
|   • Controller Layer: The Receptionist receiving requests                     |
|   • Service Layer: The Master Chefs (Calculates Risk, Simulates What-If)      |
|   • Repository Layer: The Safe Keepers talking to Database                    |
|           │                                                                   |
|           │ 4. Reads and writes permanent records                             |
|           ▼                                                                   |
|   [ DATA VAULT: Database ]                                                    |
|   • H2 In-Memory DB: Fast, zero-setup database for hackathon demos            |
|   • PostgreSQL: High-performance relational database for enterprise           |
+-------------------------------------------------------------------------------+
```

---

## 5. Teach Every New Word 📖

Let's pause and teach every important architectural term using simple everyday analogies:

| Word | What does it mean in simple words? | Everyday Real-World Example |
|---|---|---|
| **Architecture** | The organized plan of how different parts of a software system fit together. | The floor plan of a house showing where the bedrooms, kitchen, and doors are placed. |
| **Client / Frontend** | The part of the software that runs on the user's screen (browser or phone). | The steering wheel, dashboard, and pedals of a car that the driver touches. |
| **Server / Backend** | The powerful computer located far away that does the heavy calculations and enforces rules. | The powerful engine hidden underneath the car's hood. |
| **Port** | A numbered digital doorway on a computer so different programs don't mix up their mail. | Room numbers in an apartment building (e.g., Room 5173 for Frontend, Room 8080 for Backend). |
| **Reverse Proxy** | A smart front-desk gatekeeper that secretly forwards your request to the right back office. | Calling the hotel operator, who connects your call to room service without giving you the chef's personal phone number. |
| **JWT** *(JSON Web Token)* | A tamper-proof digital pass given to you when you log in, proving who you are. | A stamped hand-stamp or wristband at a concert that lets you re-enter without buying a ticket again. |
| **Controller** | The receptionist in the backend that receives incoming requests and hands them to the right worker. | The front-desk clerk at a clinic who takes your token and directs you to Doctor #3. |
| **Service** | The worker in the backend that performs the actual business logic, calculations, and rules. | The doctor who examines your symptoms and calculates your medicine dosage. |
| **Repository** | A specialized helper that writes and reads information to and from the database. | The librarian whose only job is to find the right book on the shelf and put it back. |
| **ORM / Hibernate** | A translator that turns raw database tables into easy-to-use Java objects. | An interpreter who translates English into Hindi so two people can do business smoothly. |
| **Flyway** | An automatic construction manager that creates and updates your database tables safely when the app starts. | A checklist of construction steps: Step 1: Lay the foundation. Step 2: Build the walls. |
| **In-Memory Database (H2)** | A super-fast database that lives completely inside the computer's memory (RAM) while the app is running. | A whiteboard in a meeting room: instant to write on, requires zero installation, and resets cleanly when erased. |

---

## 6. The 4 Layers of CyberShield

CyberShield is built using what software engineers call a **Layered Architecture**. Each layer has one specific responsibility:

```
[ Layer 1: PRESENTATION LAYER ]  --> React 18, Vite, CSS, Dashboards
           │ (JSON over HTTP)
[ Layer 2: SECURITY LAYER ]      --> Spring Security 6, JWT Filter, RBAC
           │ (Validated Principal)
[ Layer 3: BUSINESS LOGIC LAYER]--> Risk Engine, Optimizer, What-If, Alerts
           │ (Java Objects)
[ Layer 4: PERSISTENCE LAYER ]   --> Spring Data JPA, Hibernate, H2/Postgres
```

### Layer 1: The Presentation Layer (Frontend)
* **What technology is it?** React 18, Vite 5, Modern CSS.
* **Where does it run?** Directly inside your web browser (Chrome, Edge, Safari, Firefox).
* **What is its job?**  
  It draws the visual dashboard: the risk gauges, the 30-day trend lines, the interactive what-if sliders, and the colorful donut charts. It catches your clicks, wraps them into friendly requests, and sends them across the network.

### Layer 2: The Security Layer (The Digital Gatekeeper)
* **What technology is it?** Spring Security 6, JWT Token Parser.
* **Where does it run?** At the entrance of the backend server.
* **What is its job?**  
  Every single message arriving from the internet must pass through this gate.  
  * Does the visitor have a valid digital pass (JWT)?
  * Is the pass expired?
  * Is this user an `ADMIN` trying to approve budget, or a `VIEWER` trying to change a setting they aren't allowed to touch?  
  If anything is suspicious, this layer instantly blocks the request and replies: *"401 Unauthorized — You cannot pass!"*

### Layer 3: The Business Logic Layer (The Brain)
* **What technology is it?** Java 21, Spring Boot Services.
* **Where does it run?** Inside the backend application container.
* **What is its job?**  
  This is the pride of CyberShield! It holds all our proprietary formulas:
  1. **Risk Computation Service**: Calculates the 0–100 risk score and financial exposure in ₹.
  2. **Investment Optimizer**: Solves the mathematical budget allocation puzzle.
  3. **What-If Simulator**: Predicts the financial cost of delaying security patches.
  4. **Threat Intelligence Matcher**: Correlates hacker news with your internal servers.

### Layer 4: The Persistence Layer (The Vault)
* **What technology is it?** Spring Data JPA, Hibernate 6, Flyway, H2 / PostgreSQL.
* **Where does it run?** In the storage engine.
* **What is its job?**  
  Computers can be restarted or lose power. This layer guarantees that your list of assets, open vulnerabilities, historical risk snapshots, and user accounts are permanently saved and never lost.

---

## 7. The Journey of a Single Click: Step-by-Step 🚀

Let us trace exactly what happens inside the computer when Principal Priya clicks the **"Sign In"** button on her screen:

```
[ User types email & password ]
            │
            ▼
┌───────────────────────────────┐
│ 1. REACT FRONTEND             │
│    Gathers form inputs        │
│    Validates email format     │
└───────────────┬───────────────┘
                │ Sends POST /api/auth/login
                ▼
┌───────────────────────────────┐
│ 2. VITE DEV PROXY             │
│    Receives call on :5173     │
│    Forwards to backend :8080  │
└───────────────┬───────────────┘
                │ Same-origin forwarding
                ▼
┌───────────────────────────────┐
│ 3. SPRING SECURITY            │
│    Notices /login is public   │
│    Allows request to enter    │
└───────────────┬───────────────┘
                │ Hands to AuthController
                ▼
┌───────────────────────────────┐
│ 4. AUTH SERVICE (The Brain)   │
│    Checks database for user   │
│    Verifies BCrypt password   │
│    Creates tamper-proof JWT   │
└───────────────┬───────────────┘
                │ Queries SQL table
                ▼
┌───────────────────────────────┐
│ 5. H2 DATABASE (The Vault)    │
│    Finds "admin@abc.edu"      │
│    Returns stored hash        │
└───────────────┬───────────────┘
                │ Sends back User + JWT Token
                ▼
┌───────────────────────────────┐
│ 6. BROWSER RECEIVES TOKEN     │
│    Saves token in localStorage│
│    Redirects to /app/overview │
│    Dashboard blooms to life!  │
└───────────────────────────────┘
```

Let's walk through the 6 moments of this journey:

* **Moment 1**: Priya types `admin@abc.university` and `Admin#2026Ai`. React checks that the fields are not blank and disables the button so she doesn't accidentally double-click.
* **Moment 2**: The browser sends a secure HTTP POST message containing her credentials to `/api/auth/login`.
* **Moment 3**: The **Vite Proxy** on Port 5173 catches the message and smoothly forwards it to the Spring Boot server on Port 8080.
* **Moment 4**: The Security Gate sees that this is a login request and allows it through to `AuthController`.
* **Moment 5**: `AuthService` asks the database for Priya's record. It checks her password using **BCrypt** (a mathematical one-way shredder that verifies passwords without ever storing them in plain text). The password matches!
* **Moment 6**: `AuthService` generates a cryptographic **JWT Token** (her digital VIP pass) stamped with her user ID, organization ID, and role (`ADMIN`). The browser saves this token in `localStorage`, and the dashboard instantly appears with all college metrics!

---

## 8. Why Dual-Database? (H2 vs PostgreSQL)

A common question from hackathon judges is:
> *"What database does CyberShield use, and why?"*

CyberShield features an intelligent **Dual-Database Strategy**:

```
+-------------------------------------------------------------------------------+
|                       CYBERSHIELD DUAL-DATABASE DESIGN                        |
+-------------------------------------------------------------------------------+
|                                                                               |
|   MODE A: ZERO-CONFIG DEMO (Default)       MODE B: ENTERPRISE PRODUCTION      |
|   Database: H2 In-Memory                   Database: PostgreSQL 16+           |
|                                                                               |
|   • Lives entirely in RAM                  • Lives on hard disks / cloud      |
|   • Zero setup required                    • High concurrency & scale         |
|   • Perfect for SIH evaluation             • Production clustering            |
|   • Starts in 8 seconds on any laptop      • Stores millions of historical    |
|   • Auto-seeds realistic college data        vulnerability snapshots          |
+-------------------------------------------------------------------------------+
```

### How Flyway Makes This Seamless:
CyberShield uses an automated migration tool called **Flyway**. When the backend starts up, Flyway checks the database:
1. It runs `V1__init.sql` to create all 18 tables, primary keys, and foreign keys.
2. It runs `V2__billing_plans.sql` to insert subscription packages (`Starter`, `Growth`, `Enterprise`).
3. `DemoDataSeeder.java` detects if demo data is missing and automatically seeds the full ABC University scenario!

**Whether you run on in-memory H2 or cloud PostgreSQL, the exact same Java code and SQL migrations execute without changing a single line!**

---

## 9. Frequently Asked Questions (FAQ)

### Q1: Why did we separate Frontend (React) and Backend (Spring Boot) instead of putting everything in one file?
**Answer**: In the early days of the internet, websites mixed visual HTML and database queries in the same file. This led to disaster:
* If 10,000 students checked exam results, the web server crashed.
* If a designer wanted to change a button color, they risked deleting a database query.  
By separating React and Spring Boot, the frontend handles visual rendering on the user's computer, while the backend handles pure data processing. They can be updated, scaled, and secured independently.

### Q2: What is a "Single Page Application" (SPA)?
**Answer**: In traditional websites, every time you click a link, your screen flashes white and reloads the entire page from scratch.  
In a **Single Page Application (SPA)** like CyberShield, your browser loads the visual shell once. When you click from *Overview* to *Assets* to *What-If*, the screen updates **instantly without any white flash or page reload**. Only the small piece of new data is fetched in the background.

### Q3: What happens if the backend server restarts while using H2 database?
**Answer**: Because H2 is an in-memory database, it resets cleanly on restart. But don't worry! CyberShield's built-in `DemoDataSeeder` automatically runs on every startup, immediately populating the entire ABC University demo dataset in under 1 second. You never have to manually re-enter demo data!

### Q4: What is CORS, and why do people always complain about it?
**Answer**: **CORS** stands for *Cross-Origin Resource Sharing*. It is a security rule enforced by web browsers. If a website running on Port 5173 tries to talk to a server on Port 8080, the browser blocks it by default, suspecting a hacker attack.  
CyberShield solves this in two ways:
1. `SecurityConfig.java` has a configured CORS policy allowing local web origins.
2. The Vite dev server includes a **same-origin proxy** that routes `/api` calls internally, completely avoiding browser CORS blocks.

---

## 10. Quick 3-Question Fun Quiz! 🎯

1. **What is the job of the Spring Security filter in CyberShield?**  
   *A)* To draw colorful graphs on the screen.  
   *B)* To act like a security guard, inspecting the user's digital pass (JWT) and blocking intruders.  
   *C)* To store student exam marks on the hard drive.  

2. **Where does the H2 in-memory database store its data?**  
   *A)* On floppy disks.  
   *B)* Inside a foreign cloud datacenter.  
   *C)* Directly inside the computer's fast temporary memory (RAM).  

3. **What happens during a "Vite Proxy" request?**  
   *A)* The frontend secretly forwards API calls to the backend on the same origin so the browser doesn't block cookies or tokens.  
   *B)* The computer shuts down.  
   *C)* The user's password is sent to a public website.  

*(Answers: 1: B, 2: C, 3: A)*

---

## 11. What Did We Learn in Chapter 2? 📝

* **System Architecture** is the master blueprint that organizes CyberShield into clean, safe, and specialized rooms.
* **The Grand Hotel Analogy**:
  * **Frontend (React)** = The Lobby Reception.
  * **Security Filter (Spring Security)** = The Guard checking wristbands.
  * **Backend (Spring Boot)** = The Kitchen Chefs cooking business math.
  * **Database (H2 / Postgres)** = The Underground Storage Vault.
* **The 4 Layers**: Presentation (React), Security (JWT/RBAC), Business Logic (Risk Engine), and Persistence (JPA/Hibernate).
* **The Journey of a Click**: Every user action travels safely through input validation, proxy forwarding, security authorization, service calculation, and database storage.
* **Dual-Database Strategy**: CyberShield runs with **Zero-Config H2** for blazing fast hackathon demos, and supports **PostgreSQL** for enterprise scale.

---

*(End of Chapter 2 — System Architecture)*
