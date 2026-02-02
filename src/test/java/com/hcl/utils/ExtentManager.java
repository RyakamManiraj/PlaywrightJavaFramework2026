package com.hcl.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager {

    private static ExtentReports extent;

    private static final String TARGET_DIR = "target/";
    private static final String REPORTS_DIR = TARGET_DIR + "reports/";

    // Generate daily folder dynamically
    public static String getTodayFolder() {
        return new SimpleDateFormat("dd_MMM_yyyy").format(new Date());
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("d_MMM_yyyy_h_mm_ss_a").format(new Date());
    }

    /**
     * Singleton ExtentReports instance
     */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            try {
                createInstance(generateReportPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create ExtentReports instance", e);
            }
        }
        return extent;
    }

    private static String generateReportPath() {
        String timestamp = getTimestamp();
        String folder = getTodayFolder();
        String reportFileName = "ExtentReport_" + timestamp.replace("_", " ") + ".html";
        return REPORTS_DIR + folder + "/" + reportFileName;
    }

    private static void createInstance(String reportPath) throws IOException {
        // Create daily folder if not exists
        Path reportFolder = Paths.get(REPORTS_DIR, getTodayFolder());
        Files.createDirectories(reportFolder);

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Playwright Automation Execution Report");
        spark.config().setReportName("Playwright Test Execution");
        spark.config().setTimelineEnabled(true);
        spark.config().setEncoding("UTF-8");

        Path xmlConfigPath = Paths.get("src/test/resources/extent-config.xml");
        if (Files.exists(xmlConfigPath)) {
            spark.loadXMLConfig(xmlConfigPath.toFile());
        }

        extent = new ExtentReports();
        extent.attachReporter(spark);

        // Add system info
        extent.setSystemInfo("Framework", "Playwright Java");
        extent.setSystemInfo("Tester", "Maniraj");
        extent.setSystemInfo("Environment", ConfigReader.getProperty("env", "QA"));
        extent.setSystemInfo("Base URL", ConfigReader.getProperty("baseUrl", "https://example.com"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", ConfigReader.getProperty("browser", "chromium"));
    }
}
