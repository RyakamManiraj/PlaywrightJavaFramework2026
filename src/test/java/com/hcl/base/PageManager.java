package com.hcl.base;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;

/**
 * PageManager is a lightweight utility class responsible for managing
 * the current Playwright {@link Page} and {@link Frame} instances
 * in a thread-safe manner using {@link ThreadLocal}.
 * <p>
 * This class acts as a central access point for Page and Frame objects,
 * ensuring:
 * <ul>
 *   <li>Each parallel test thread works with its own Page instance</li>
 *   <li>Frame switching does not affect other tests</li>
 *   <li>Clean isolation between test executions</li>
 * </ul>
 *
 * PageBase and TestBase rely on this class to retrieve the active
 * page or frame context during test execution.
 */
public class PageManager {

    /**
     * ThreadLocal storage for Playwright Page instance.
     * Ensures thread safety during parallel execution.
     */
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    /**
     * ThreadLocal storage for Playwright Frame instance.
     * Used to track the currently active frame per test thread.
     */
    private static final ThreadLocal<Frame> tlFrame = new ThreadLocal<>();

    /**
     * Sets the current Page instance for the executing thread.
     * <p>
     * When a new page is set, any previously stored frame reference
     * is cleared to avoid stale frame usage.
     *
     * @param page Playwright {@link Page} instance
     */
    public static void setPage(Page page) {
        tlPage.set(page);
        tlFrame.remove(); // reset frame when a new page is set
    }

    /**
     * Returns the Page instance associated with the current thread.
     *
     * @return current {@link Page}
     */
    public static Page getPage() {
        return tlPage.get();
    }

    /**
     * Clears Page and Frame references from ThreadLocal storage.
     * <p>
     * This method is typically called during test teardown to ensure
     * proper cleanup and prevent memory leaks.
     */
    public static void unload() {
        tlPage.remove();
        tlFrame.remove();
    }

    // ------------------------ Frame management ------------------------

    /**
     * Sets the active Frame for the current thread.
     *
     * @param frame Playwright {@link Frame} to switch to
     */
    public static void setFrame(Frame frame) {
        tlFrame.set(frame);
    }

    /**
     * Returns the active Frame for the current thread.
     * <p>
     * If no frame has been explicitly set, this method defaults
     * to returning the main frame of the current Page.
     *
     * @return active {@link Frame} instance
     */
    public static Frame getFrame() {
        Frame frame = tlFrame.get();
        return frame != null ? frame : tlPage.get().mainFrame(); // default to main frame
    }
}
