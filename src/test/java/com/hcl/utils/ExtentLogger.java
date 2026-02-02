package com.hcl.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * ExtentLogger - Thread-safe logger for ExtentReports + Playwright screenshots.
 * True flicker-free screenshots using hidden headless page.
 */
public class ExtentLogger {

    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThread = new ThreadLocal<>();

    private static final String screenshotMode = ConfigReader
            .getProperty("reporting.screenshots", "Failed")
            .trim()
            .toLowerCase(Locale.ROOT);

    private static final boolean FULL_PAGE =
            Boolean.parseBoolean(ConfigReader.getProperty("screenshot.fullpage", "false"));

    private static BrowserContext hiddenContext;
    private static Page hiddenPage;
    private static boolean flickerFreeMode = true;

    // ---------- Folder Setup ----------
    private static final String BASE_DIR = "target/screenshots/";
    private static final String SCREENSHOTS_DIR = BASE_DIR + "Screenshots/";
    private static final String LATEST_DIR = BASE_DIR + "LatestScreenshots/";

    public static final String RUN_TIMESTAMP =
            new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss").format(new Date());
    private static final Path RUN_FOLDER = Paths.get(SCREENSHOTS_DIR, RUN_TIMESTAMP);

    static {
        try {
            Files.createDirectories(RUN_FOLDER);
            Files.createDirectories(Paths.get(LATEST_DIR));
            Files.list(Paths.get(LATEST_DIR)).forEach(file -> {
                try { Files.delete(file); } catch (Exception ignored) {}
            });
        } catch (Exception e) {
            System.err.println("[ExtentLogger] Folder setup failed: " + e.getMessage());
        }
    }

    // ---------- Hidden Context Init ----------
    public static void initHiddenContext(Browser browser) {
        Page page = getPage();
        if (page == null) return;

        try {
            if (flickerFreeMode && hiddenContext == null) {
                hiddenContext = browser.newContext();
                hiddenPage = hiddenContext.newPage();

                // Navigate hidden page to current page URL
                String currentUrl = page.url();
                hiddenPage.navigate(currentUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                // Sync scroll position
                double scrollY = (double) page.evaluate("() => window.scrollY");
                hiddenPage.evaluate("y => window.scrollTo(0, y)", scrollY);

                System.out.println("[ExtentLogger] 🧹 Hidden context initialized.");
            }
        } catch (Exception e) {
            System.err.println("[ExtentLogger] ⚠️ initHiddenContext failed: " + e.getMessage());
        }
    }

    public static void closeHiddenContext() {
        try {
            if (hiddenPage != null) { hiddenPage.close(); hiddenPage = null; }
            if (hiddenContext != null) { hiddenContext.close(); hiddenContext = null; }
            System.out.println("[ExtentLogger] 🧹 Hidden context closed.");
        } catch (Exception e) {
            System.err.println("[ExtentLogger] ❌ Failed to close hidden context: " + e.getMessage());
        }
    }

    // ---------- Test/Page Binding ----------
    public static void setTest(ExtentTest test) { testThread.set(test); }
    public static ExtentTest getTest() { return testThread.get(); }
    public static void removeTest() { testThread.remove(); }

    public static void setPage(Page page) { pageThread.set(page); }
    public static Page getPage() { return pageThread.get(); }
    public static void removePage() { pageThread.remove(); }

    // ---------- Screenshot Logic ----------
    private static boolean shouldAttach(String event) {
        switch (screenshotMode) {
            case "all": return true;
            case "failed": return "fail".equalsIgnoreCase(event);
            case "passed": return "pass".equalsIgnoreCase(event);
            default: return false;
        }
    }

    private static void attachScreenshotToTest(ExtentTest test, Page page, String name) {
        if (test == null || page == null) return;

        try {
            String timestamp = new SimpleDateFormat("HH_mm_ss").format(new Date());
            String fileName = name + "_" + timestamp + ".png";
            Path screenshotPath = RUN_FOLDER.resolve(fileName);

            Page targetPage = page;

            if (flickerFreeMode && hiddenPage != null) {
                targetPage = hiddenPage;

                // Clone DOM from main page
                String dom = (String) page.evaluate("() => document.documentElement.outerHTML");
                hiddenPage.setContent(dom);

                // Wait until all resources load
                hiddenPage.waitForLoadState(LoadState.NETWORKIDLE);

                // Disable animations/transitions
                hiddenPage.addStyleTag(new Page.AddStyleTagOptions()
                        .setContent("* { transition: none !important; animation: none !important; }"));
            }

            // Capture screenshot
            byte[] screenshotBytes = targetPage.screenshot(new Page.ScreenshotOptions().setFullPage(FULL_PAGE));
            Files.write(screenshotPath, screenshotBytes);

            // Copy to Latest folder
            Path latestPath = Paths.get(LATEST_DIR, fileName);
            Files.copy(screenshotPath, latestPath, StandardCopyOption.REPLACE_EXISTING);

            // Attach to Extent
            String base64 = Base64.getEncoder().encodeToString(screenshotBytes);
            test.info("📸 Screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64, name).build());

        } catch (Exception e) {
            System.err.println("[ExtentLogger] attachScreenshotToTest failed: " + e.getMessage());
        }
    }

    public static void attachScreenshot(Page page, String name) {
        ExtentTest test = getTest();
        attachScreenshotToTest(test, page, name);
    }

    // ---------- Console Log Utility ----------
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    private static String now() { return new SimpleDateFormat("HH:mm:ss").format(new Date()); }

    private static void log(String emoji, String level, String color, String message) {
        System.out.printf("%s%s [%s] %s %s%s%n", color, emoji, now(), level, message, RESET);
    }

    // ---------- Logging APIs ----------
    public static void info(String message) {
        ExtentTest test = getTest();
        if (test != null) test.info(message);
        log("ℹ️", "[INFO]", BLUE, message);
        if (shouldAttach("info")) attachScreenshotToTest(test, getPage(), "Info");
    }

    public static void pass(String message) {
        ExtentTest test = getTest();
        if (test != null) test.pass(message);
        log("✅", "[PASS]", GREEN, message);
        if (shouldAttach("pass")) attachScreenshotToTest(test, getPage(), "Pass");
    }

    public static void fail(String message) {
        ExtentTest test = getTest();
        if (test != null) test.fail(message);
        log("❌", "[FAIL]", RED, message);
        if (shouldAttach("fail")) attachScreenshotToTest(test, getPage(), "Fail");
    }

    public static void warning(String message) {
        ExtentTest test = getTest();
        if (test != null) test.warning(message);
        log("⚠️", "[WARN]", YELLOW, message);
        if (shouldAttach("warning")) attachScreenshotToTest(test, getPage(), "Warning");
    }
}
