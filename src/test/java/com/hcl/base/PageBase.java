package com.hcl.base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import com.hcl.utils.ConfigReader;
import com.hcl.utils.ExtentLogger;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.*;

/**
 * PageBase is the core base class for all Page Object classes in the Playwright Java framework.
 * <p>
 * This class provides:
 * <ul>
 *   <li>Thread-safe access to Playwright {@link Page} using {@link PageManager}</li>
 *   <li>Common wait utilities for synchronization</li>
 *   <li>Reusable element interaction methods (click, type, hover, etc.)</li>
 *   <li>Frame handling, alerts handling, and JavaScript execution</li>
 *   <li>Screenshot utilities for reporting and debugging</li>
 * </ul>
 *
 * All page classes should extend this class to inherit standardized behavior,
 * better maintainability, and consistent logging across the framework.
 */
public class PageBase {

    /**
     * Configuration reader instance used to fetch values from config files.
     */
    protected ConfigReader config;

    /**
     * Initializes the PageBase and loads configuration properties.
     */
    public PageBase() {
        this.config = new ConfigReader();
    }

    // ------------------------ Thread-safe Page ------------------------

    /**
     * Returns the current Playwright Page instance from ThreadLocal storage.
     *
     * @return active {@link Page} instance
     * @throws RuntimeException if Page is not initialized for the current thread
     */
    protected Page getPage() {
        Page page = PageManager.getPage();
        if (page == null) throw new RuntimeException("Page is not initialized in ThreadLocal!");
        return page;
    }

    /**
     * Returns a Playwright {@link Locator} for the given selector after ensuring
     * the element is present in the DOM.
     *
     * @param selector CSS/XPath selector of the element
     * @return {@link Locator} instance
     */
    public Locator getLocator(String selector) {
        waitForPresence(selector);
        return getPage().locator(selector);
    }

    /**
     * Retrieves a frame from the page using name or selector.
     *
     * @param nameOrSelector frame name or selector
     * @return {@link Frame} instance
     * @throws RuntimeException if frame is not found
     */
    protected Frame getFrame(String nameOrSelector) {
        Frame frame = getPage().frame(nameOrSelector);
        if (frame == null) throw new RuntimeException("Frame not found: " + nameOrSelector);
        return frame;
    }

    // ------------------------ Waits ------------------------

    /**
     * Waits until the element becomes visible using default timeout.
     *
     * @param selector element selector
     */
    public void waitForVisibility(String selector) {
        waitForVisibility(selector, getDefaultTimeout());
    }

    /**
     * Waits until the element becomes visible within the specified timeout.
     *
     * @param selector element selector
     * @param timeoutMs timeout in milliseconds
     */
    public void waitForVisibility(String selector, int timeoutMs) {
        try {
            getLocator(selector).waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            ExtentLogger.pass("Element visible: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Failed waiting for visibility: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Waits until the element is attached to the DOM using default timeout.
     *
     * @param selector element selector
     */
    public void waitForPresence(String selector) {
        waitForPresence(selector, getDefaultTimeout());
    }

    /**
     * Waits until the element is attached to the DOM using a custom timeout.
     *
     * @param selector element selector
     * @param timeoutMs timeout in milliseconds
     */
    public void waitForPresence(String selector, int timeoutInMs) {
        try {
            getPage().waitForSelector(selector, new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(timeoutInMs));
            ExtentLogger.pass("Element present in DOM: " + selector);
        } catch (PlaywrightException e) {
            ExtentLogger.fail("Failed waiting for presence: " + selector + " | " + e);
            throw e;
        }
    }


    /**
     * Waits until the element becomes invisible or hidden.
     *
     * @param selector element selector
     */
    public void waitForInvisibility(String selector) {
        try {
            getPage().waitForSelector(selector,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(getDefaultTimeout()));
            ExtentLogger.pass("Element hidden: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Failed waiting for invisibility: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Forces the execution to wait for a fixed amount of time.
     *
     * @param ms time in milliseconds
     */
    public void waitForTimeout(int ms) {
        getPage().waitForTimeout(ms);
        ExtentLogger.info("Waited for " + ms + " ms");
    }

    // ------------------------ Element Actions ------------------------

    /**
     * Clicks on the specified element.
     *
     * @param selector element selector
     */
    public void click(String selector) {
        try {
            getLocator(selector).click();
            ExtentLogger.pass("Clicked on element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Click failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Performs a double-click action on the element.
     *
     * @param selector element selector
     */
    public void doubleClick(String selector) {
        try {
            getLocator(selector).dblclick();
            ExtentLogger.pass("Double-clicked element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Double-click failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Performs a right-click (context click) on the element.
     *
     * @param selector element selector
     */
    public void rightClick(String selector) {
        try {
            getLocator(selector).click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
            ExtentLogger.pass("Right-clicked element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Right-click failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Clears the existing value and types text into the element.
     *
     * @param selector element selector
     * @param text text to be typed
     */
    @SuppressWarnings("deprecation")
    public void type(String selector, String text) {
        try {
            Locator element = getLocator(selector);
            element.fill("");
            element.type(text);
            ExtentLogger.pass("Typed '" + text + "' into element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Type failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Fills the element directly with the provided text.
     *
     * @param selector element selector
     * @param text value to fill
     */
    public void fill(String selector, String text) {
        try {
            getLocator(selector).fill(text);
            ExtentLogger.pass("Filled '" + text + "' in element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Fill failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Presses a keyboard key on the specified element.
     *
     * @param selector element selector
     * @param key key to press (e.g., Enter, Tab)
     */
    public void pressKey(String selector, String key) {
        try {
            getLocator(selector).press(key);
            ExtentLogger.pass("Pressed key '" + key + "' on element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Press key failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Scrolls to the element and focuses the mouse pointer on it.
     *
     * @param selector element selector
     */
    public void focusOnElement(String selector) {
        try {
            // Scroll the element into view
            getLocator(selector).scrollIntoViewIfNeeded();

            // Move mouse over the element (focus)
            getLocator(selector).hover();

            ExtentLogger.pass("Focused on element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Focus failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Moves the mouse pointer over the element.
     *
     * @param selector element selector
     */
    public void hover(String selector) {
        try {
            getLocator(selector).hover();
            ExtentLogger.pass("Hovered over element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Hover failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Clears text from an input, textarea, or editable element.
     * Works for normal fields and iframe editors.
     *
     * @param locator Playwright selector for the element
     */
    public void clear(String locator) {
        try {
            // Try standard fill (best for input & textarea)
            getPage().locator(locator).fill("");
        } catch (Exception e) {
            // Fallback for contenteditable elements (like TinyMCE)
            getPage().locator(locator).evaluate("el => el.textContent = ''");
        }
    }

    /**
     * Scrolls the page until the element is visible.
     *
     * @param selector element selector
     */
    public void scrollToElement(String selector) {
        try {
            getLocator(selector).scrollIntoViewIfNeeded();
            ExtentLogger.pass("Scrolled to element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Scroll failed: " + selector + " | " + e.getMessage());
        }
    }

    /**
     * Scrolls to bottom of the page.
     */
    public void scrollToBottom() {
        executeJS("window.scrollTo(0, document.body.scrollHeight)");
    }

    // ------------------------ Dropdown / Select ------------------------

    /**
     * Selects a dropdown option using value attribute.
     */
    public void selectByValue(String selector, String value) {
        getLocator(selector).selectOption(value);
        ExtentLogger.pass("Selected value '" + value + "' from dropdown: " + selector);
    }

    /**
     * Selects a dropdown option using visible label.
     */
    public void selectByLabel(String selector, String label) {
        getLocator(selector).selectOption(new SelectOption().setLabel(label));
        ExtentLogger.pass("Selected label '" + label + "' from dropdown: " + selector);
    }

    /**
     * Selects a dropdown option using index position.
     */
    public void selectByIndex(String selector, int index) {
        getLocator(selector).selectOption(new SelectOption().setIndex(index));
        ExtentLogger.pass("Selected index '" + index + "' from dropdown: " + selector);
    }

    /**
     * Returns the currently selected dropdown value.
     */
    public String getSelectedDropdownValue(String selector) {
        return getLocator(selector).inputValue();
    }

    // ------------------------ Frames ------------------------

    /**
     * Switches execution context to the specified frame.
     */
    public Frame switchToFrame(String frameNameOrId) {
        Frame frame = getPage().frame(frameNameOrId);
        if (frame == null) {
            throw new RuntimeException("Frame not found: " + frameNameOrId);
        }
        ExtentLogger.info("Switched to frame: " + frameNameOrId);
        return frame;
    }

    /**
     * Switches execution context back to the main page.
     */
    public void switchToDefaultFrame() {
        PageManager.setFrame(null);
        ExtentLogger.info("Switched to default Frame");
    }

    /**
     * Retrieves visible text from an element inside a specified iframe.
     *
     * @param frameSelector the name or CSS/XPath selector of the iframe
     * @param elementSelector the selector of the element inside the iframe
     * @return the inner text of the element
     */
    public String getTextFromFrame(Frame frame, String elementSelector) {
        try {
            Locator element = frame.locator(elementSelector);
            element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            String text = element.innerText();
            ExtentLogger.pass("Got text from frame '" + frame.name() + "': '" + text + "' from element: " + elementSelector);
            return text;

        } catch (Exception e) {
            ExtentLogger.fail("Failed to get text from element: " + elementSelector + " inside frame: " + frame.name() + " | " + e.getMessage());
            throw e;
        }
    }


    /**
     * Gets the Playwright Frame object by its frame name.
     *
     * @param frameName the name attribute of the frame
     * @return Frame object if found
     * @throws RuntimeException if frame is not found
     */
    public Frame getFrameByName(String frameName) {
        try {
            Frame frame = getPage().frame(frameName);

            if (frame == null) {
                ExtentLogger.fail("Frame not found: " + frameName);
                throw new RuntimeException("Frame not found: " + frameName);
            }

            ExtentLogger.info("Switched to frame: " + frameName);
            return frame;

        } catch (Exception e) {
            ExtentLogger.fail("Failed to switch to frame: " + frameName + " | " + e.getMessage());
            throw e;
        }
    }


    
    // ------------------------ Alerts / Dialogs ------------------------

    /**
     * Handles browser alert, confirm, or prompt dialogs.
     *
     * @param accept whether to accept or dismiss the alert
     * @param textToSend text to send for prompt dialogs (optional)
     */
    public void handleAlert(boolean accept, String textToSend) {
        getPage().onceDialog(dialog -> {
            if (textToSend != null) dialog.accept(textToSend);
            else if (accept) dialog.accept();
            else dialog.dismiss();
            ExtentLogger.info("Handled alert: " + dialog.message());
        });
    }

    // ------------------------ JavaScript ------------------------

    /**
     * Executes JavaScript in the context of the current page.
     */
    public Object executeJS(String script, Object... args) {
        return getPage().evaluate(script, args);
    }

    // ------------------------ Getters ------------------------

    /**
     * Retrieves visible text from an element.
     */
    public String getText(String selector) {
        try {
            String text = getLocator(selector).innerText();
            ExtentLogger.pass("Got text: '" + text + "' from element: " + selector);
            return text;
        } catch (Exception e) {
            ExtentLogger.fail("Get text failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the value of a specified attribute from an element.
     */
    public String getAttribute(String selector, String attribute) {
        try {
            String attr = getLocator(selector).getAttribute(attribute);
            ExtentLogger.pass("Got attribute '" + attribute + "' = '" + attr + "' from element: " + selector);
            return attr;
        } catch (Exception e) {
            ExtentLogger.fail("Get attribute failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks whether the element is visible on the page.
     */
    public boolean isElementVisible(String selector) {
        try {
            boolean visible = getLocator(selector).isVisible();
            ExtentLogger.pass("Element visibility: " + selector + " | Visible: " + visible);
            return visible;
        } catch (Exception e) {
            ExtentLogger.warning("Visibility check failed: " + selector + " | " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the element is enabled for interaction.
     */
    public boolean isElementEnabled(String selector) {
        try {
            boolean enabled = getLocator(selector).isEnabled();
            ExtentLogger.pass("Element enabled: " + selector + " | Enabled: " + enabled);
            return enabled;
        } catch (Exception e) {
            ExtentLogger.warning("Enabled check failed: " + selector + " | " + e.getMessage());
            return false;
        }
    }

    // ------------------------ Screenshots ------------------------

    /**
     * Captures a full-page screenshot and stores it in execution-specific and latest folders.
     */
    public String captureFullPageScreenshot(String testName) {
        try {
            String timestamp = getTimestamp();
            String fileName = testName + "_" + timestamp + ".png";

            Path runFolder = Paths.get("target/screenshots/Screenshots", ExtentLogger.RUN_TIMESTAMP);
            Path latestFolder = Paths.get("target/screenshots/LatestScreenshots");
            Files.createDirectories(runFolder);
            Files.createDirectories(latestFolder);

            Path screenshotPath = runFolder.resolve(fileName);
            Path latestCopyPath = latestFolder.resolve(fileName);

            byte[] bytes = getPage().screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Files.write(screenshotPath, bytes);
            Files.copy(screenshotPath, latestCopyPath, StandardCopyOption.REPLACE_EXISTING);

            return screenshotPath.toString();
        } catch (Exception e) {
            ExtentLogger.warning("Full page screenshot failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot of a specific element.
     */
    public String captureElementScreenshot(String selector, String testName) {
        try {
            String timestamp = getTimestamp();
            String fileName = testName + "_element_" + timestamp + ".png";

            Path runFolder = Paths.get("target/screenshots/Screenshots", ExtentLogger.RUN_TIMESTAMP);
            Path latestFolder = Paths.get("target/screenshots/LatestScreenshots");
            Files.createDirectories(runFolder);
            Files.createDirectories(latestFolder);

            Path screenshotPath = runFolder.resolve(fileName);
            Path latestCopyPath = latestFolder.resolve(fileName);

            byte[] bytes = getLocator(selector).screenshot();
            Files.write(screenshotPath, bytes);
            Files.copy(screenshotPath, latestCopyPath, StandardCopyOption.REPLACE_EXISTING);

            return screenshotPath.toString();
        } catch (Exception e) {
            ExtentLogger.warning("Element screenshot failed: " + e.getMessage());
            return null;
        }
    }

    // ------------------------ Utilities ------------------------

    /**
     * Returns the default explicit wait timeout from configuration.
     */
    protected int getDefaultTimeout() {
        return Integer.parseInt(ConfigReader.getProperty("explicitWait", "5000"));
    }

    /**
     * Generates a timestamp string used for file naming.
     */
    protected String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    // ------------------------ Browser Navigation ------------------------

    /**
     * Navigates to the given URL and waits until the page is fully loaded.
     *
     * @param url Application URL to navigate
     */
    public void navigateTo(String url) {
        try {
            getPage().navigate(url);
            getPage().waitForLoadState();
            ExtentLogger.pass("Navigated to URL: " + url);
        } catch (Exception e) {
            ExtentLogger.fail("Navigation failed for URL: " + url + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Navigates one step backward in browser history.
     */
    public void browserBack() {
        try {
            getPage().goBack();
            ExtentLogger.pass("Navigated back in browser history");
        } catch (Exception e) {
            ExtentLogger.fail("Browser back navigation failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Navigates one step forward in browser history.
     */
    public void browserForward() {
        try {
            getPage().goForward();
            ExtentLogger.pass("Navigated forward in browser history");
        } catch (Exception e) {
            ExtentLogger.fail("Browser forward navigation failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Refreshes (reloads) the current page.
     */
    public void browserRefresh() {
        try {
            getPage().reload();
            ExtentLogger.pass("Browser page refreshed");
        } catch (Exception e) {
            ExtentLogger.fail("Browser refresh failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Refreshes the page and waits for load completion.
     */
    public void browserRefreshAndWait() {
        try {
            getPage().reload();
            getPage().waitForLoadState();
            ExtentLogger.pass("Browser refreshed and page load completed");
        } catch (Exception e) {
            ExtentLogger.fail("Browser refresh and wait failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns the current page URL.
     *
     * @return current URL
     */
    public String getCurrentUrl() {
        try {
            String url = getPage().url();
            ExtentLogger.pass("Fetched current URL: " + url);
            return url;
        } catch (Exception e) {
            ExtentLogger.fail("Failed to fetch current URL | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns the current page title.
     *
     * @return page title
     */
    public String getPageTitle() {
        try {
            String title = getPage().title();
            ExtentLogger.pass("Fetched page title: " + title);
            return title;
        } catch (Exception e) {
            ExtentLogger.fail("Failed to fetch page title | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Waits for the page to reach a specific load state.
     *
     * @param state Playwright LoadState (LOAD, DOMCONTENTLOADED, NETWORKIDLE)
     */
    public void waitForPageLoad(LoadState state) {
        try {
            getPage().waitForLoadState(state);
            ExtentLogger.pass("Page reached load state: " + state);
        } catch (Exception e) {
            ExtentLogger.fail("Waiting for page load state failed | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ Additional Element State Checks ------------------------

    /**
     * Checks whether the element is present in the DOM (attached).
     */
    public boolean isElementPresent(String selector) {
        try {
            boolean present = getPage().locator(selector).count() > 0;
            ExtentLogger.pass("Element presence check: " + selector + " | Present: " + present);
            return present;
        } catch (Exception e) {
            ExtentLogger.warning("Presence check failed: " + selector + " | " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the element is disabled.
     */
    public boolean isElementDisabled(String selector) {
        try {
            boolean disabled = getLocator(selector).isDisabled();
            ExtentLogger.pass("Element disabled: " + selector + " | Disabled: " + disabled);
            return disabled;
        } catch (Exception e) {
            ExtentLogger.warning("Disabled check failed: " + selector + " | " + e.getMessage());
            return false;
        }
    }

    // ------------------------ Checkbox / Radio Actions ------------------------

    /**
     * Checks a checkbox or radio button if not already checked.
     */
    public void check(String selector) {
        try {
            Locator element = getLocator(selector);
            if (!element.isChecked()) {
                element.check();
            }
            ExtentLogger.pass("Checked element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Check failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Unchecks a checkbox if it is checked.
     */
    public void uncheck(String selector) {
        try {
            Locator element = getLocator(selector);
            if (element.isChecked()) {
                element.uncheck();
            }
            ExtentLogger.pass("Unchecked element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Uncheck failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ File Upload ------------------------

    /**
     * Uploads a file using input[type='file'].
     */
    public void uploadFile(String selector, String filePath) {
        try {
            getLocator(selector).setInputFiles(Paths.get(filePath));
            ExtentLogger.pass("Uploaded file: " + filePath + " using element: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("File upload failed: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ Mouse & Keyboard Utilities ------------------------

    /**
     * Scrolls the page using mouse wheel.
     */
    public void scrollBy(int x, int y) {
        try {
            getPage().mouse().wheel(x, y);
            ExtentLogger.pass("Scrolled page by X: " + x + ", Y: " + y);
        } catch (Exception e) {
            ExtentLogger.fail("Scroll by mouse failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Presses a keyboard key without focusing on any element.
     */
    public void pressGlobalKey(String key) {
        try {
            getPage().keyboard().press(key);
            ExtentLogger.pass("Pressed global key: " + key);
        } catch (Exception e) {
            ExtentLogger.fail("Global key press failed | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ Window / Tab Handling ------------------------

    /**
     * Waits for a new browser tab or window to open and switches to it.
     */
    public Page waitForNewTabAndSwitch() {
        try {
            Page newPage = getPage().context().waitForPage(() -> {});
            PageManager.setPage(newPage);
            ExtentLogger.pass("Switched to new browser tab/window");
            return newPage;
        } catch (Exception e) {
            ExtentLogger.fail("Failed to switch to new tab | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Switches to a window based on its title.
     *
     * @param title expected window title
     */
    public void switchToWindowByTitle(String title) {
        try {
            for (Page page : getPage().context().pages()) {
                if (page.title().equals(title)) {
                    PageManager.setPage(page);
                    ExtentLogger.pass("Switched to window with title: " + title);
                    return;
                }
            }
            throw new RuntimeException("Window not found with title: " + title);
        } catch (Exception e) {
            ExtentLogger.fail("Switch window by title failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Switches to a window based on its URL.
     *
     * @param url expected window URL
     */
    public void switchToWindowByUrl(String url) {
        try {
            for (Page page : getPage().context().pages()) {
                if (page.url().contains(url)) {
                    PageManager.setPage(page);
                    ExtentLogger.pass("Switched to window with URL containing: " + url);
                    return;
                }
            }
            throw new RuntimeException("Window not found with URL: " + url);
        } catch (Exception e) {
            ExtentLogger.fail("Switch window by URL failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns all open windows/tabs in the current context.
     */
    public List<Page> getAllWindowHandles() {
        return getPage().context().pages();
    }

    /**
     * Closes all other windows/tabs except the current active one.
     */
    public void closeAllOtherWindows() {
        Page current = getPage();
        for (Page page : current.context().pages()) {
            if (!page.equals(current)) {
                page.close();
            }
        }
        ExtentLogger.pass("Closed all other windows except current");
    }

    /**
     * Switches to window by index.
     *
     * @param index index of window (0-based)
     */
    public void switchToWindowByIndex(int index) {
        List<Page> pages = getPage().context().pages();
        if (index < 0 || index >= pages.size()) {
            throw new RuntimeException("Invalid window index: " + index);
        }
        PageManager.setPage(pages.get(index));
        ExtentLogger.pass("Switched to window at index: " + index);
    }

    /**
     * Closes window by index.
     *
     * @param index index of window (0-based)
     */
    public void closeWindowByIndex(int index) {
        List<Page> pages = getPage().context().pages();
        if (index < 0 || index >= pages.size()) {
            throw new RuntimeException("Invalid window index: " + index);
        }
        pages.get(index).close();
        ExtentLogger.pass("Closed window at index: " + index);
    }

    /**
     * Waits for a new tab to open and switches to it.
     * Returns the new Page instance.
     */
    public Page switchToNewTab() {
        try {
            Page newPage = getPage().context().waitForPage(() -> {});
            PageManager.setPage(newPage);
            ExtentLogger.pass("Switched to new tab");
            return newPage;
        } catch (Exception e) {
            ExtentLogger.fail("Switch to new tab failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Waits for a new window to open and switches to it.
     * Returns the new Page instance.
     */
    public Page switchToNewWindow() {
        try {
            Page newWindow = getPage().context().waitForPage(() -> {});
            PageManager.setPage(newWindow);
            ExtentLogger.pass("Switched to new window");
            return newWindow;
        } catch (Exception e) {
            ExtentLogger.fail("Switch to new window failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Switches back to the parent window (first tab).
     */
    public void switchToParentWindow() {
        try {
            Page parentPage = getPage().context().pages().get(0);
            PageManager.setPage(parentPage);
            ExtentLogger.pass("Switched back to parent window");
        } catch (Exception e) {
            ExtentLogger.fail("Switch to parent window failed | " + e.getMessage());
            throw e;
        }
    }

    /**
     * Closes current tab and switches to previous tab.
     */
    public void closeCurrentTab() {
        try {
            Page current = getPage();
            current.close();

            Page previous = current.context().pages().get(0);
            PageManager.setPage(previous);
            ExtentLogger.pass("Closed current tab and switched to previous");
        } catch (Exception e) {
            ExtentLogger.fail("Close tab failed | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ Additional Utilities ------------------------

    /**
     * Waits until element is enabled and clickable.
     *
     * @param selector element selector
     */
    public void waitForEnabled(String selector) {
        waitForVisibility(selector);
        getLocator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(getDefaultTimeout()));
    }

    /**
     * Waits for an element to be enabled and visible.
     *
     * @param selector element selector
     */
    public void waitForElementEnabled(String selector) {
        try {
            waitForVisibility(selector);
            getLocator(selector).waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(getDefaultTimeout()));
            ExtentLogger.pass("Element is enabled: " + selector);
        } catch (Exception e) {
            ExtentLogger.fail("Element not enabled: " + selector + " | " + e.getMessage());
            throw e;
        }
    }

    public void waitForElementToBeVisibleAndClickable(String selector) {
        waitForVisibility(selector);
        waitForElementEnabled(selector);
    }

    /**
     * Waits for element to disappear from page.
     *
     * @param selector element selector
     */
    public void waitForElementToDisappear(String selector) {
        waitForInvisibility(selector);
    }

    /**
     * Performs click action based on click type using JavaScript.
     *
     * @param selector element selector (CSS or XPath)
     * @param type     click type: "click", "doubleClick", "rightClick", "index"
     * @param index    index for list elements (only used when type = "index")
     */
    public void clickWithType(String selector, String type, int index) {
        try {
            switch (type.toLowerCase()) {
                case "click":
                    executeJS("document.querySelector('" + selector + "').click();");
                    break;

                case "doubleclick":
                    executeJS("var el = document.querySelector('" + selector + "');" +
                              "el.dispatchEvent(new MouseEvent('dblclick', {bubbles: true, cancelable: true}));");
                    break;

                case "rightclick":
                    executeJS("var el = document.querySelector('" + selector + "');" +
                              "el.dispatchEvent(new MouseEvent('contextmenu', {bubbles: true, cancelable: true}));");
                    break;

                case "index":
                    executeJS("document.querySelectorAll('" + selector + "')[" + index + "].click();");
                    break;

                default:
                    throw new IllegalArgumentException("Invalid click type: " + type);
            }

            ExtentLogger.pass("Clicked element with type: " + type + " | Selector: " + selector);

        } catch (Exception e) {
            ExtentLogger.fail("JS Click failed | Type: " + type + " | Selector: " + selector + " | Error: " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ Assertions ------------------------

    /**
     * Verifies that element text equals the expected value.
     */
    public void verifyTextEquals(String selector, String expectedText) {
        String actualText = getText(selector);
        if (!actualText.equals(expectedText)) {
            ExtentLogger.fail("Text mismatch | Expected: " + expectedText + " | Actual: " + actualText);
            throw new AssertionError("Text verification failed");
        }
        ExtentLogger.pass("Text verified successfully: " + expectedText);
    }

    /**
     * Verifies element text contains expected value.
     *
     * @param selector element selector
     * @param expectedText expected text value
     */
    public void verifyTextContains(String selector, String expectedText) {
        String actual = getText(selector);
        if (!actual.contains(expectedText)) {
            ExtentLogger.fail("Text mismatch | Expected to contain: " + expectedText + " | Actual: " + actual);
            throw new AssertionError("Text verification failed");
        }
        ExtentLogger.pass("Text verified contains: " + expectedText);
    }

    /**
     * Pauses execution for the specified duration (hard wait).
     *
     * @param milliseconds time to pause in milliseconds
     */
    public void pause(long milliseconds) {
        try {
            getPage().waitForTimeout(milliseconds);
            ExtentLogger.info("Execution paused for " + milliseconds + " ms");
        } catch (Exception e) {
            ExtentLogger.fail("Pause execution failed | " + e.getMessage());
            throw e;
        }
    }

    // ------------------------ RANDOM TEXT / DATA ------------------------

    /**
     * Generates random alphanumeric text.
     *
     * @param length number of characters required
     * @return random string
     */
    public String getRandomText(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generates random email using random text.
     *
     * @return random email string
     */
    public String getRandomEmail() {
        return "auto_" + getRandomText(8) + "@testmail.com";
    }

    /**
     * Generates random numeric string.
     *
     * @param digits number of digits required
     * @return random number string
     */
    public String getRandomNumber(int digits) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // ------------------------ DATE METHODS ------------------------

    /**
     * Returns current date in specified format.
     *
     * @param format date format (e.g., "dd/MM/yyyy")
     * @return formatted date string
     */
    public String getCurrentDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    /**
     * Returns date after specified days in custom format.
     *
     * @param days number of days to add
     * @param format date format
     * @return formatted date string
     */
    public String getDateAfterDays(int days, String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    /**
     * Returns date before specified days in custom format.
     *
     * @param days number of days to subtract
     * @param format date format
     * @return formatted date string
     */
    public String getDateBeforeDays(int days, String format) {
        return getDateAfterDays(-days, format);
    }
}
