# 📘 Playwright Java Automation Framework — README (Updated 2026)

This README provides a **complete guide** to understanding, setting up, and running the Playwright Java Automation Framework.  
It includes updated folder structures, examples, and references from your current project.

***

# 🧰 Tools & Technologies

| Tool                    | Purpose                                        |
| ----------------------- | ---------------------------------------------- |
| **Java (JDK 11+)**      | Core language                                  |
| **Playwright for Java** | Browser automation (Chromium, Firefox, WebKit) |
| **TestNG**              | Test execution & suite configuration           |
| **Maven**               | Build system & dependency manager              |
| **ExtentReports**       | Rich HTML reporting                            |
| **Apache POI**          | Excel test‑data reading                        |
| **Jackson**             | JSON parsing                                   |
| **OpenCSV**             | CSV parsing                                    |
| **ThreadLocal APIs**    | Thread‑safety & parallel execution             |

***

# 💻 System Requirements

*   **Java JDK:** 11 or higher (recommended: 17)
*   **Maven:** 3.8+
*   **IDE:** IntelliJ IDEA / Eclipse
*   **OS:** Windows / macOS / Linux
*   **Internet required for Playwright browser download**

Check versions:

```bash
java -version
mvn -version
```

***

# ⚙️ Project Setup

### 1️⃣ Clone Repository

```bash
git clone <repo-url>
cd Playwright_Java_Framework
```

### 2️⃣ Install Dependencies

```bash
mvn clean install
```

This downloads:

*   Playwright drivers & browsers
*   Maven libraries

### 3️⃣ Configure Test Settings

Modify:

    src/test/resources/config.properties

Example:

```properties
browser=chromium
headless=false
baseUrl=https://the-internet.herokuapp.com/
reporting.screenshots=Failed
screenshot.fullpage=true
```

***

# 📂 Updated Project Structure (Based on Your Latest Files)

    Playwright_Java_Framework
    │
    ├── src/main/java
    │   └── (Framework utilities)
    │
    ├── src/test/java
    │   ├── com.hcl.base
    │   │   ├── PageBase.java
    │   │   ├── PageManager.java
    │   │   └── TestBase.java
    │   │
    │   ├── com.hcl.pages
    │   │   └── InternetHerokuAppPage.java
    │   │
    │   ├── com.hcl.tests
    │   │   ├── InternetHerokuAppE2ETests.java
    │   │   ├── LoginTC_Csv.java
    │   │   ├── LoginTC_Excel.java
    │   │   ├── LoginTC_Json.java
    │   │   └── LoginTC_XML.java
    │   │
    │   └── com.hcl.utils
    │       ├── ConfigReader.java
    │       ├── DynamicDataProvider.java
    │       ├── ExtentLogger.java
    │       ├── ExtentManager.java
    │       └── TestDataFile.java
    │
    ├── src/test/resources
    │   ├── testdata
    │   │   ├── Login.csv
    │   │   ├── Login.json
    │   │   ├── Login.xlsx
    │   │   └── Login.xml
    │   ├── config.properties
    │   └── extent-config.xml
    │
    └── target
        ├── reports
        │   ├── LatestReport/ExtentReport.html
        │   └── 28_Jan_2026/ExtentReport_28 Jan.html
        │
        ├── screenshots
        │   ├── LatestScreenshots/
        │   └── 28_Jan_2026/...PNG files
        │
        ├── traces
        │   └── 28_Jan_2026/*.zip Playwright trace files
        │
        └── videos
            └── 28_Jan_2026/*.webm video files

***

# 🧠 Framework Architecture

### Execution Flow

    TestNG Test Class
            ↓
    TestBase (Browser + Context + Tracing)
            ↓
    PageManager (ThreadLocal Page)
            ↓
    PageBase (Reusable actions)
            ↓
    Page Objects (POM)
            ↓
    ExtentLogger (Logs + Screenshot)
            ↓
    HTML Report (Extent)

***

# 🧩 Core Classes Overview

### **TestBase.java**

*   Creates browser
*   Creates context/page
*   Enables:
    *   Video recording
    *   Playwright tracing
*   Attaches browser to **ThreadLocal PageManager**

***

### **PageBase.java**

Reusable wrapper actions:

*   click
*   type
*   waitForVisible
*   scroll
*   hover
*   getText

***

### **PageManager.java**

*   Thread-safe Page & Locator access
*   One page per test thread
*   Required for parallel execution

***

# 🧪 Test Structure

Your updated project includes:

*   Login test using **CSV**
*   Login test using **Excel**
*   Login test using **JSON**
*   Login test using **XML**
*   End‑to‑end HerokuApp tests

Example Data‑Driven Test:

```java
@Test(dataProvider = "testData", dataProviderClass = DynamicDataProvider.class)
@TestDataFile(file = "src/test/resources/testdata/Login.xlsx")
public void loginTest(Map<String, String> data) {
    login(data.get("username"), data.get("password"));
}
```

***

# 📊 Data Formats Supported

*   ✔️ Excel
*   ✔️ CSV
*   ✔️ JSON
*   ✔️ XML

All handled via **DynamicDataProvider.java**.

***

# 📸 Reports, Screenshots, Videos & Traces

| Artifact               | Location                                |
| ---------------------- | --------------------------------------- |
| **Extent HTML Report** | `target/reports/LatestReport/`          |
| **Screenshots**        | `target/screenshots/LatestScreenshots/` |
| **Video Recording**    | `target/videos/`                        |
| **Tracing ZIP files**  | `target/traces/`                        |

Playwright captures:

*   failures
*   pass images (optional)
*   full-page screenshots

***

# ▶️ How to Run

**All tests:**

```bash
mvn test
```

**Single test class:**

```bash
mvn -Dtest=LoginTC_Excel test
```

**Using TestNG XML:**

```bash
mvn test -DsuiteXmlFile=testng.xml
```

***

# 🚀 CI/CD Ready

This framework supports:

*   GitHub Actions
*   Jenkins
*   Azure DevOps
*   Docker execution

***

# ✔️ Final Notes

This is a **scalable**, **data-driven**, **high-performance** Playwright Java framework built using:

*   Clean architecture
*   Thread-safe execution
*   Modular design
*   Cross-browser support

Perfect for enterprise-level testing.

***
