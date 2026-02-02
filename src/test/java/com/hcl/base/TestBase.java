package com.hcl.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.*;
import com.hcl.utils.ConfigReader;
import com.hcl.utils.ExtentLogger;
import com.hcl.utils.ExtentManager;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TestBase is the central base class for all TestNG test classes in the
 * framework.
 * <p>
 * It is responsible for:
 * <ul>
 * <li>Initializing Playwright, Browser, BrowserContext, and Page in a
 * thread-safe manner</li>
 * <li>Managing browser configuration (browser type, headless mode, base
 * URL)</li>
 * <li>Integrating ExtentReports for detailed test reporting</li>
 * <li>Handling test lifecycle events (setup, teardown, suite
 * initialization)</li>
 * <li>Capturing screenshots, videos, and Playwright traces for debugging</li>
 * </ul>
 *
 * Each test method runs in its own isolated browser session using ThreadLocal
 * variables, enabling safe parallel execution.
 */
public class TestBase {

	// -------------------- ThreadLocal Playwright Objects --------------------

	/** ThreadLocal Playwright instance for parallel execution */
	private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();

	/** ThreadLocal Browser instance */
	private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();

	/** ThreadLocal BrowserContext instance */
	private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();

	/** ThreadLocal Page instance */
	private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

	// --------- Browser override from test case ---------

	/** Allows overriding browser type from individual test cases */
	private static ThreadLocal<String> browserTypeTL = new ThreadLocal<>();

	/** Allows overriding headless mode from individual test cases */
	private static ThreadLocal<Boolean> headlessTL = new ThreadLocal<>();

	/** Allows overriding application URL from individual test cases */
	private static ThreadLocal<String> urlTL = new ThreadLocal<>();

	/**
	 * Sets the browser type (chrome, firefox, edge, chromium) for the current test
	 * thread.
	 */
	public static void setBrowser(String browser) {
		browserTypeTL.set(browser);
	}

	// -------------------- Config & Test Details --------------------

	/** Configuration reader instance */
	protected ConfigReader config;

	/** Base application URL */
	protected String baseUrl;

	/** Headless execution flag */
	protected boolean headless;

	/** ExtentReports instance shared across suite */
	protected static ExtentReports extent;

	/** ExtentTest instance for current test */
	protected ExtentTest test;

	/** Folder name based on current execution date */
	protected static String todayFolder;

	/** Directory path for recorded videos */
	protected static String videosDir;

	/** Directory path for Playwright trace files */
	protected static String tracesDir;

	// -------------------- SUITE SETUP --------------------

	/**
	 * Executes once before the entire test suite starts.
	 * <p>
	 * Responsibilities:
	 * <ul>
	 * <li>Loads configuration properties</li>
	 * <li>Initializes ExtentReports</li>
	 * <li>Creates base directories for videos and traces</li>
	 * </ul>
	 */
	@BeforeSuite(alwaysRun = true)
	public void beforeSuiteSetup() {
		System.out.println("\n========== TEST SUITE STARTED ==========" + "\n");

		// Load properties FIRST so ExtentLogger won't fail
		ConfigReader.loadProperties("src/test/resources/config.properties");
		extent = ExtentManager.getInstance();

		todayFolder = ExtentManager.getTodayFolder();
		videosDir = "target/videos/" + todayFolder + "/";
		tracesDir = "target/traces/" + todayFolder + "/";
		createDirectory(videosDir);
		createDirectory(tracesDir);
	}

	/** Sets headless mode override for the current test */
	public static void setHeadless(boolean headless) {
		headlessTL.set(headless);
	}

	/** Sets base URL override for the current test */
	public static void setUrl(String url) {
		urlTL.set(url);
	}

	/** Returns headless override value for current thread */
	public static Boolean getHeadless() {
		return headlessTL.get();
	}

	/** Returns URL override value for current thread */
	public static String getUrl() {
		return urlTL.get();
	}

	// -------------------- TEST SETUP --------------------

	/**
	 * Executes before each @Test method.
	 * <p>
	 * Responsibilities:
	 * <ul>
	 * <li>Reads configuration and applies ThreadLocal overrides</li>
	 * <li>Initializes ExtentTest for the current test method</li>
	 * <li>Launches Playwright browser and context</li>
	 * <li>Starts video recording and tracing</li>
	 * <li>Navigates to the application URL</li>
	 * </ul>
	 */
	@BeforeMethod(alwaysRun = true)
	public void setup(Method method) {
		try {
			// Initialize config
			ExtentLogger.info("▶️ Test STARTED: " + method.getName());
			config = new ConfigReader();
			baseUrl = config.getProperty("baseUrl");

			// Default headless from config
			boolean configHeadless = Boolean.parseBoolean(config.getProperty("headless", "false"));

			// ThreadLocal overrides (set from test if needed)
			String browserType = browserTypeTL.get() != null ? browserTypeTL.get()
					: config.getProperty("browser", "chrome");
			boolean headlessValue = headlessTL.get() != null ? headlessTL.get()
					: Boolean.parseBoolean(config.getProperty("headless", "false"));
			String urlValue = urlTL.get() != null ? urlTL.get() : config.getProperty("baseUrl");

			// Start ExtentTest
			String authorName = config.getProperty("author", System.getProperty("user.name"));
			test = extent.createTest(method.getName()).assignAuthor(authorName);
			ExtentLogger.setTest(test);
			ExtentLogger.info("### Browser selected = " + browserType + " | Headless = " + headlessValue + " | URL = "
					+ urlValue);

			// Launch Playwright browser
			Playwright pw = Playwright.create();
			Browser br;

			BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headlessValue);

			if (headlessValue) {
				launchOptions.setArgs(List.of("--headless=new"));
			}

			switch (browserType.toLowerCase()) {
			case "firefox":
				br = pw.firefox().launch(launchOptions);
				break;
			case "edge":
				launchOptions.setChannel("msedge");
				br = pw.chromium().launch(launchOptions);
				break;
			case "chromium":
				br = pw.chromium().launch(launchOptions);
				break;
			case "chrome":
			default:
				launchOptions.setChannel("chrome");
				br = pw.chromium().launch(launchOptions);
				break;
			}

			// Video directory per test
			String videoDir = videosDir + method.getName() + "_" + getReadableTimestamp();
			createDirectory(videoDir);

			BrowserContext ctx = br.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900)
					.setRecordVideoDir(Paths.get(videoDir)).setIgnoreHTTPSErrors(true));

			Page pg = ctx.newPage();

			// Store objects in ThreadLocal
			tlPlaywright.set(pw);
			tlBrowser.set(br);
			tlContext.set(ctx);
			tlPage.set(pg);

			PageManager.setPage(pg);
			ExtentLogger.setPage(pg);

			// Start tracing
			ctx.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));

			pg.navigate(urlValue);
			ExtentLogger.pass("Browser started: " + browserType + " | URL: " + urlValue);

		} catch (Exception e) {
			ExtentLogger.fail("Browser launch failed: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	// -------------------- TEST TEARDOWN --------------------

	/**
	 * Executes after each @Test method.
	 * <p>
	 * Responsibilities:
	 * <ul>
	 * <li>Updates test status in ExtentReports</li>
	 * <li>Captures screenshots on failure</li>
	 * <li>Stops tracing and saves trace file</li>
	 * <li>Closes browser, context, and Playwright</li>
	 * <li>Cleans ThreadLocal references</li>
	 * </ul>
	 */
	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		String testName = result.getName();
		try {
			Page pg = tlPage.get();
			BrowserContext ctx = tlContext.get();
			Browser br = tlBrowser.get();
			Playwright pw = tlPlaywright.get();

			switch (result.getStatus()) {
			case ITestResult.FAILURE:
				ExtentLogger.fail("❌ Test Failed: " + result.getThrowable());
				ExtentLogger.attachScreenshot(pg, testName);
				break;
			case ITestResult.SUCCESS:
				ExtentLogger.pass("✅ Test Passed Successfully");
				break;
			case ITestResult.SKIP:
				ExtentLogger.warning("⚠️ Test Skipped: " + result.getThrowable());
				break;
			}

			String traceFile = tracesDir + testName + "_" + getReadableTimestamp() + "_trace.zip";
			ctx.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(traceFile)));

			ctx.close();
			br.close();
			pw.close();

			renameLatestVideo(testName);

		} catch (Exception e) {
			ExtentLogger.fail("Error during teardown: " + e.getMessage());
		} finally {
			tlPage.remove();
			tlContext.remove();
			tlBrowser.remove();
			tlPlaywright.remove();
			PageManager.unload();

			extent.flush();
			ExtentLogger.removeTest();
			ExtentLogger.removePage();
		}
		ExtentLogger.info("⏹️ Test ENDED: " + result.getName());
		System.out.println("");
		System.out.println("");
	}

	// -------------------- SUITE TEARDOWN --------------------

	/**
	 * Executes once after the entire test suite completes.
	 * <p>
	 * Finalizes ExtentReports and copies the latest HTML report to a common
	 * "LatestReport" directory.
	 */
	@AfterSuite(alwaysRun = true)
	public void afterSuiteTearDown() {
		try {
			ExtentLogger.info("Finalizing Extent Report...");
			if (extent != null)
				extent.flush();

			Path reportsDir = Paths.get("target/reports");
			Path latestReportPath = null;
			List<Path> htmlReports = new ArrayList<>();
			Files.walk(reportsDir).filter(p -> p.toString().endsWith(".html")).forEach(htmlReports::add);

			long latestTime = 0;
			for (Path p : htmlReports) {
				long modifiedTime = p.toFile().lastModified();
				if (modifiedTime > latestTime) {
					latestTime = modifiedTime;
					latestReportPath = p;
				}
			}

			if (latestReportPath != null) {
				Path latestFolder = Paths.get("target/reports/LatestReport");
				Files.createDirectories(latestFolder);
				Path destination = latestFolder.resolve("ExtentReport.html");
				Files.copy(latestReportPath, destination, StandardCopyOption.REPLACE_EXISTING);
			}
			System.out.println("");
			System.out.println("\n========== TEST SUITE COMPLETED ==========" + "\n");

		} catch (Exception e) {
			System.err.println("⚠️ Error while copying latest report: " + e.getMessage());
		}
	}

	@BeforeClass(alwaysRun = true)
	public void beforeTestClass() {
	    String className = this.getClass().getSimpleName();
		System.out.println("");
	    ExtentLogger.info("🚀 Testcase STARTED: " + className);
	}

	@AfterClass(alwaysRun = true)
	public void afterTestClass() {
	    String className = this.getClass().getSimpleName();
	    ExtentLogger.info("🏁 Testcase ENDED: " + className);
		System.out.println("");
	}

	
	// -------------------- UTILITIES --------------------

	/** Creates a directory if it does not already exist */
	private static void createDirectory(String dir) {
		try {
			Files.createDirectories(Paths.get(dir));
		} catch (Exception e) {
			System.err.println("Failed to create directory: " + dir);
		}
	}

	/** Returns a human-readable timestamp for file naming */
	protected static String getReadableTimestamp() {
		return new SimpleDateFormat("d_MMM_yyyy_h_mm_ss_a", Locale.ENGLISH).format(new Date());
	}

	/**
	 * Renames the most recently recorded video file to include test name and
	 * timestamp for easier identification.
	 */
	private void renameLatestVideo(String testName) {
		try {
			File folder = new File(videosDir);
			File[] files = folder.listFiles((dir, name) -> name.endsWith(".webm"));
			if (files == null || files.length == 0)
				return;

			File latestVideo = Arrays.stream(files).max(Comparator.comparingLong(File::lastModified)).orElse(null);

			if (latestVideo != null) {
				String newName = videosDir + testName + "_" + getReadableTimestamp() + ".webm";
				latestVideo.renameTo(new File(newName));
				ExtentLogger.info("🎥 Video saved as: " + newName);
			}
		} catch (Exception e) {
			ExtentLogger.warning("Video rename failed: " + e.getMessage());
		}
	}

	// -------------------- THREAD-LOCAL GETTERS --------------------

	/** Returns Playwright instance for current thread */
	public static Playwright getPlaywright() {
		return tlPlaywright.get();
	}

	/** Returns Browser instance for current thread */
	public static Browser getBrowser() {
		return tlBrowser.get();
	}

	/** Returns BrowserContext instance for current thread */
	public static BrowserContext getContext() {
		return tlContext.get();
	}

	/** Returns Page instance for current thread */
	public static Page getPage() {
		return tlPage.get();
	}
}
