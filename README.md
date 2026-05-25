# MediCore HMS — Setup Guide
## JavaFX + Spring Boot + MySQL (No JSON / No REST)

---

## Architecture at a Glance

```
JavaFX UI (LoginScreen / DashboardScreen)
    │  Java method calls (NO HTTP, NO JSON)
    ▼
Spring Boot Services (PatientService / BedService)
    │  Spring @Transactional
    ▼
Spring Data JPA Repositories (PatientRepository / BedRepository)
    │  Hibernate auto-generates SQL
    ▼
MySQL Database (medicore_db → patients + beds tables)
```

---

## Prerequisites

| Tool        | Version  | Download                          |
|-------------|----------|-----------------------------------|
| Java JDK    | 17+      | https://adoptium.net              |
| Maven       | 3.8+     | https://maven.apache.org          |
| MySQL       | 8.0+     | https://dev.mysql.com/downloads/  |
| IntelliJ    | Any      | https://www.jetbrains.com/idea/   |

---

## Step 1 — Create MySQL Database

Open MySQL Workbench or terminal and run:

```sql
-- Create the database (tables are auto-created by Hibernate)
CREATE DATABASE IF NOT EXISTS medicore_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;
-- You should see: medicore_db
```

That's it. Hibernate will auto-create `patients` and `beds` tables on first run.

---

## Step 2 — Configure Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medicore_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username= root          # ← your MySQL username
spring.datasource.password= Anuja@25 # ← your MySQL password
```

---

## Step 3 — Open in IntelliJ IDEA

1. File → Open → select the `medicore-hms` folder
2. IntelliJ detects `pom.xml` automatically → click **"Load Maven Project"**
3. Wait for Maven to download dependencies (~2 min first time)

---

## Step 4 — Run the Application

### Option A — IntelliJ
Right-click `MediCoreApp.java` → **Run 'MediCoreApp.main()'**

### Option B — Maven terminal
```bash
cd medicore-hms
mvn javafx:run
```

### Option C — IntelliJ Run Configuration
1. Run → Edit Configurations → + → Application
2. Main class: `com.medicore.MediCoreApp`
3. VM options: `--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml`

---

## Step 5 — First Run Behaviour

On first launch:
1. Spring Boot starts, connects to MySQL
2. Hibernate creates `patients` and `beds` tables automatically
3. `BedService.initializeBedsIfEmpty()` seeds 20 beds into MySQL
4. Login screen appears

Login: **admin** / **admin123**

---

## Project Structure

```
medicore-hms/
├── pom.xml                             ← Maven dependencies
└── src/main/
    ├── java/com/medicore/
    │   ├── MediCoreApp.java            ← JavaFX entry point, starts Spring
    │   ├── SpringBootConfig.java       ← @SpringBootApplication
    │   ├── model/
    │   │   ├── Patient.java            ← @Entity → MySQL `patients` table
    │   │   └── Bed.java                ← @Entity → MySQL `beds` table
    │   ├── repository/
    │   │   ├── PatientRepository.java  ← JPA interface, auto-SQL
    │   │   └── BedRepository.java      ← JPA interface, auto-SQL
    │   ├── service/
    │   │   ├── PatientService.java     ← Business logic, @Transactional
    │   │   └── BedService.java         ← Business logic, @Transactional
    │   └── ui/
    │       ├── LoginScreen.java        ← JavaFX login page
    │       └── DashboardScreen.java    ← JavaFX main dashboard
    └── resources/
        └── application.properties      ← DB credentials & JPA config
```

---

## How Connectivity Works (No JSON)

### Admit a Patient
```
User clicks "Admit Patient" button (DashboardScreen.java)
  → Calls: patientService.admitPatient(id, name, age, ward, bedId, diagnosis)
      → Hibernate executes: INSERT INTO patients (...) VALUES (...)
      → Hibernate executes: UPDATE beds SET status='OCCUPIED' WHERE bed_id=?
  → UI refreshed from DB: bedService.getAllBeds() + patientService.getAllPatients()
```

### Discharge a Patient
```
User clicks "Discharge" button in table
  → Calls: patientService.dischargePatient(patientId)
      → Hibernate: UPDATE patients SET status='Discharged', exit_date=TODAY WHERE patient_id=?
      → Hibernate: UPDATE beds SET status='AVAILABLE' WHERE bed_id=?
  → UI refreshed automatically
```

### JavaFX Threading Model
All DB calls run on a **background thread** (JavaFX `Task<>`).
All UI updates run on the **JavaFX Application Thread** (`Platform.runLater()`).
This prevents the UI from freezing during database operations.

---

## MySQL Tables (Auto-Created)

### `beds` table
| Column              | Type         | Description              |
|---------------------|--------------|--------------------------|
| id                  | BIGINT PK    | Auto-increment           |
| bed_id              | VARCHAR(10)  | e.g. "B-01" (unique)     |
| ward                | VARCHAR(50)  | General/ICU/Pediatric/Orthopedic |
| status              | VARCHAR(20)  | AVAILABLE or OCCUPIED    |
| current_patient_id  | VARCHAR(20)  | Patient ID if occupied   |

### `patients` table
| Column      | Type          | Description               |
|-------------|---------------|---------------------------|
| id          | BIGINT PK     | Auto-increment            |
| patient_id  | VARCHAR(20)   | e.g. "P-0001" (unique)    |
| name        | VARCHAR(100)  | Full name                 |
| age         | INT           | Age                       |
| ward        | VARCHAR(50)   | Assigned ward             |
| bed_id      | VARCHAR(10)   | Assigned bed              |
| diagnosis   | VARCHAR(200)  | Medical diagnosis         |
| entry_date  | DATE          | Admission date            |
| exit_date   | DATE          | Discharge date (nullable) |
| status      | VARCHAR(20)   | Admitted or Discharged    |

---

## Common Errors & Fixes

| Error | Fix |
|-------|-----|
| `Communications link failure` | MySQL not running → start it |
| `Access denied for user 'root'` | Wrong password in `application.properties` |
| `Unknown database 'medicore_db'` | Run `CREATE DATABASE medicore_db;` in MySQL |
| JavaFX `Module not found` | Add VM args in Run Configuration |
| `Port 3306 already in use` | Another MySQL instance running |

---

## Verify Data in MySQL

After admitting a patient, check MySQL directly:
```sql
USE medicore_db;
SELECT * FROM patients;
SELECT * FROM beds WHERE status = 'OCCUPIED';
```
PS C:\Medicore> cd C:\Medicore
PS C:\Medicore> mvn javafx:run

to kill any port process
anetstat -ano | findstr :8080
taskkill /PID 27136 /F



start backend
cd C:\Users\anuja\Downloads\medicore-frontend
PS C:\Users\anuja\Downloads\medicore-frontend> npm start


start frontend
cd C:\Medicore
PS C:\Medicore> mvn spring-boot:run


JAR is now at:
C:\Medicore\target\medicore-hms-1.0.0.jar