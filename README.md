# PlaywrightJavaFramework2026

# 📘 Playwright Java Automation Framework – README

This document explains the **end‑to‑end flow, setup, tools, and usage** of the Playwright Java Automation Framework. It is written so that **any new user can clone the project and start execution with minimal guidance**.

---

## 🧰 Tools & Technologies Used

| Tool / Library | Purpose |
|---------------|---------|
| **Java (JDK 11+)** | Core programming language |
| **Playwright for Java** | Browser automation (Chromium, Firefox, WebKit) |
| **TestNG** | Test execution, annotations, grouping, data providers |
| **Maven** | Build & dependency management |
| **ExtentReports** | Rich HTML reporting |
| **Apache POI** | Excel test‑data reading |
| **Jackson / Gson** | JSON parsing |
| **OpenCSV** | CSV data support |
| **ThreadLocal** | Parallel execution safety |

---

## 💻 System Requirements

- **Java JDK:** 11 or higher (recommended: JDK 17)
- **Maven:** 3.8+
- **IDE:** IntelliJ IDEA / Eclipse
- **OS:** Windows / macOS / Linux
- **Internet Access:** Required for Playwright browser downloads

Verify installations:
```bash
java -version
mvn -version
```

---

## ⚙️ Framework Setup Instructions

### 1️⃣ Clone Repository
```bash
git clone <your-repo-url>
cd Playwright_Java_Framework
```

### 2️⃣ Install Dependencies
```bash
mvn clean install
```
This will:
- Download Maven dependencies
- Install Playwright browsers automatically

### 3️⃣ Configure Application

Edit **`src/test/resources/config.properties`**
```properties
browser=firefox
headless=false
baseUrl=https://the-internet.herokuapp.com/
reporting.screenshots=Failed
screenshot.fullpage=true
```

---

## 📂 Project Structure Explained

```
Playwright_Java_Framework
│
├── src/main/java
│   └── (framework core utilities)
│
├── src/test/java
│   ├── com.hcl.base
│   │   ├── PageBase.java        # Common UI actions (click, type, wait, scroll)
│   │   ├── PageManager.java     # ThreadLocal Page & Frame manager
│   │   └── TestBase.java        # Browser + context lifecycle
│   │
│   ├── com.hcl.pages
│   │   └── HerokuAppPage.java   # Page Object Model (locators + actions)
│   │
│   ├── com.hcl.tests
│   │   ├── HerokuAppTest.java   # Login tests
│   │   ├── HerokuAppTest2.java  # Additional flows
│   │   └── CheckboxDropdownTest.java # Checkbox & dropdown tests
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
│   │   ├── Login.xlsx
│   │   ├── Login.csv
│   │   ├── Login.json
│   │   └── Login.xml
│   ├── config.properties
│   └── extent-config.xml
│
└── target
    ├── reports
    │   └── LatestReport/ExtentReport.html
    ├── screenshots
    ├── videos
    └── traces
```

---

## 🧠 Framework Architecture & Flow

### 🔹 Test Execution Flow

```
TestNG Test
   ↓
TestBase
   ↓
Playwright Browser Launch
   ↓
PageManager (ThreadLocal Page)
   ↓
Page Object (HerokuAppPage)
   ↓
PageBase Actions
   ↓
ExtentLogger (Logs + Screenshots)
   ↓
Extent HTML Report
```

---

## 🧩 Base Layer Responsibilities

### 📌 TestBase.java
- Launches browser (Chromium / Firefox / WebKit)
- Controls headless mode
- Creates browser context, page, tracing, video
- Attaches page to **PageManager**

### 📌 PageManager.java
- Thread‑safe `Page` and `Frame` storage
- Supports iframe switching
- Ensures parallel execution safety

### 📌 PageBase.java
Reusable UI actions:
- `click()`
- `type()`
- `getText()`
- `hover()`
- `scrollToElement()`
- `waitForVisible()`

---

## 🧪 Page Object Model (POM)

### Example: HerokuAppPage.java

- Stores **locators only**
- Exposes **business‑level actions**
- No assertions inside page classes

Supported features:
- Login
- Checkboxes
- Dropdowns
- Hover
- Scroll

---

## 📊 Data‑Driven Testing

Supported formats:
- ✅ Excel (`.xlsx`)
- ✅ CSV
- ✅ JSON
- ✅ XML

### Usage Example
```java
@Test(dataProvider = "testData", dataProviderClass = DynamicDataProvider.class)
@TestDataFile(file = "src/test/resources/testdata/Login.xlsx")
public void testLogin(Map<String, String> data) {
    login(data.get("username"), data.get("password"));
}
```

---

## ☑️ Checkbox & Dropdown Data‑Driven Testing

### Checkbox Test Logic
- Reads checkbox states from test data
- Verifies checked / unchecked status

### Dropdown Test Logic
- Reads dropdown values from data file
- Selects by value
- Asserts selected option

---

## 📸 Reporting & Screenshots

### ExtentLogger Features
- Thread‑safe logging
- Auto screenshots on:
  - Pass
  - Fail
  - Info (configurable)
- Flicker‑free screenshots using hidden browser

### Report Location
```
target/reports/LatestReport/ExtentReport.html
```

---

## 🎥 Video, Traces & Screenshots

| Artifact | Location |
|--------|---------|
| Screenshots | `target/screenshots/` |
| Videos | `target/videos/` |
| Traces | `target/traces/` |

---

## ▶️ How to Run Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn -Dtest=HerokuAppTest test
```

### Run via TestNG XML
```bash
mvn test -DsuiteXmlFile=testng.xml
```

---

## 🚀 Best Practices Followed

- Clean Page Object Model
- Zero test logic duplication
- Thread‑safe execution
- Config‑driven execution
- Scalable for CI/CD
- Easy onboarding for new testers

---

## 📌 Final Notes

This framework is **enterprise‑ready**, **scalable**, and **CI friendly**. It supports modern automation practices with Playwright while keeping TestNG flexibility and Extent reporting clarity.

---
