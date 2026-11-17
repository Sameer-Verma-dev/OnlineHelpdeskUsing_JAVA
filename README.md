# Online Complaint and Helpdesk System
**Course:** CSE2003 | Object Oriented Programming using JAVA | B.Tech CSE Semester 3 | Mini Project  
**Group:** Group 11  
**Project Description:** Log service complaints, automate SLA escalation, and monitor resolution workflows.

---

## 👥 Project Team Members (Group 11)

| Role | Name | UID / Roll Number | Status |
| :--- | :--- | :--- | :--- |
| **Project Leader** | **Himanshu** | 2510001185 | Active |
| **Core Developer** | **Shubham** | 2510001171 | Active |
| **Core Developer** | **Sameer** | 2510001164 | Active |
| **Core Developer** | **Khushveer** | 2510001153 | Active |

---

## 📋 Core Modules

1. **User Registration & Authentication**: Multi-role login & registration for Students/Customers (`USER`), Support Agents (`AGENT`), and Administrators (`ADMIN`).
2. **Complaint Submission**: Dynamic ticket logging for multiple problem categories (`Hardware`, `Software`, `Service`, `General`) with initial priority setting, automatic SLA calculation, and field validation.
3. **Ticket Assignment**: Support agents or admins can assign pending complaints to specialized support engineers via the `Assignable` interface.
4. **Status and Priority Updates**: Lifecycle workflow (`OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`), priority management (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), and resolution note logging.
5. **Resolution and Feedback**: Customers submit 1–5 star ratings and reviews for resolved complaints, with aggregated helpdesk satisfaction metrics.
6. **Automated SLA Escalation**: Dedicated background thread continuously monitoring open tickets, automatically elevating priority for overdue requests.

---

## 🏗️ Technical Architecture & Design Implementation

| System Component | Design Pattern / Capability | Implementation Details |
| :--- | :--- | :--- |
| **User & Ticket Modeling** | Encapsulation & Domain Modeling | `User`, `Ticket`, `Agent`, `Admin`, `Customer` model classes with private fields, constructors, and access methods. |
| **Ticket Hierarchy** | Inheritance & Polymorphic Dispatch | • `Assignable` interface implemented by `Ticket`.<br/>• Abstract base `Ticket` class with polymorphic subclasses: `HardwareTicket`, `SoftwareTicket`, `ServiceTicket`, and `GeneralTicket`.<br/>• Dynamic SLA calculation via `calculateSlaHours()`. |
| **Validation & Escalation Engine** | Exception Handling & Multithreading | • Custom `InvalidTicketException` for field validation and illegal status transitions.<br/>• **Automated Escalation Service** (`TicketEscalationService`): Background daemon thread that periodically scans active tickets and auto-escalates unresolved tickets exceeding SLA limits. |
| **Database Persistence** | Data Access Object (DAO) & JDBC | Standard JDBC with SQLite (`helpdesk.db`): `DBConnection`, `UserDAO`, `TicketDAO`, `FeedbackDAO` using `PreparedStatement` and parameterized queries. |
| **Desktop GUI Dashboard** | Event-Driven Architecture (Swing) | Interactive desktop GUI with card panels, custom tables, colored status/priority badges, filter bars, live escalation console, and dialogs. |

---

## 🗄️ Database Schema (`schema.sql`)

The database consists of 3 relational tables:
1. **`users`**:
   - `id` (INTEGER PRIMARY KEY)
   - `username` (VARCHAR UNIQUE)
   - `password` (VARCHAR)
   - `full_name` (VARCHAR)
   - `email` (VARCHAR)
   - `role` (`USER`, `AGENT`, `ADMIN`)
   - `department_or_phone` (VARCHAR)
   - `created_at` (TIMESTAMP)

2. **`tickets`**:
   - `id` (INTEGER PRIMARY KEY)
   - `ticket_code` (VARCHAR UNIQUE, e.g. `TCK-1001`)
   - `user_id` (INTEGER FOREIGN KEY ➔ users.id)
   - `customer_name` (VARCHAR)
   - `title` (VARCHAR)
   - `description` (TEXT)
   - `category` (`HARDWARE`, `SOFTWARE`, `SERVICE`, `GENERAL`)
   - `priority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
   - `status` (`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`)
   - `assigned_agent_id` (INTEGER FOREIGN KEY ➔ users.id)
   - `assigned_agent_name` (VARCHAR)
   - `specific_detail_1`, `specific_detail_2`, `specific_detail_3` (VARCHAR)
   - `escalated` (INTEGER 0 or 1)
   - `resolution_notes` (TEXT)
   - `created_at`, `updated_at` (TIMESTAMP)

3. **`feedback`**:
   - `id` (INTEGER PRIMARY KEY)
   - `ticket_id` (INTEGER FOREIGN KEY ➔ tickets.id)
   - `ticket_code` (VARCHAR)
   - `user_id` (INTEGER FOREIGN KEY ➔ users.id)
   - `user_name` (VARCHAR)
   - `rating` (INTEGER 1–5)
   - `comments` (TEXT)
   - `created_at` (TIMESTAMP)

---

## 🚀 How to Run the Project

### Quick Run
Double-click `run.bat` or run:
```cmd
run.bat
```

### Full Rebuild and Run
Double-click `build_and_run.bat` or run:
```cmd
build_and_run.bat
```

---

## 🔑 Pre-seeded Demo Credentials

Click any of the **Quick Demo Login** buttons on the login screen or enter:

| Role | Username | Password | User Name / Details |
| :--- | :--- | :--- | :--- |
| **Project Leader (Customer)** | `himanshu` | `user123` | Himanshu (Student / Customer) |
| **Support Agent 1** | `shubham` | `agent123` | Shubham (Hardware Support) |
| **Support Agent 2** | `sameer` | `agent123` | Sameer (Software Support) |
| **Support Agent 3** | `khushveer` | `agent123` | Khushveer (Network & Service) |
| **Administrator** | `admin` | `admin123` | System Administrator |
