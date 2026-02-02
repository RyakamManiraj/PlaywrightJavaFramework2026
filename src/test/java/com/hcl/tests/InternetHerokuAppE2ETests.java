package com.hcl.tests;

import com.hcl.base.TestBase;
import com.hcl.pages.InternetHerokuAppPage;
import com.hcl.utils.DynamicDataProvider;
import com.hcl.utils.ExtentLogger;
import com.hcl.utils.TestDataFile;
import com.microsoft.playwright.Frame;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InternetHerokuAppE2ETests extends TestBase {

	InternetHerokuAppPage page;

	@BeforeClass
	public void setup() {
		TestBase.setBrowser("chrome");
		TestBase.setHeadless(true);
		TestBase.setUrl("https://the-internet.herokuapp.com/");
		page = new InternetHerokuAppPage();
	}

	@Test
	public void testNavigationAndVisibility() {
		page.navigateTo("https://the-internet.herokuapp.com/");
		page.waitForTimeout(1000);

		Assert.assertTrue(page.isElementVisible(page.loginLink));
		page.waitForTimeout(1000);

		page.goToLogin();
		page.waitForTimeout(1000);

	}

	@Test
	public void testLoginAndFlashMessage() {
		page.goToLogin();
		page.waitForTimeout(1000);

		page.type(page.usernameInput, "tomsmith");
		page.waitForTimeout(1000);

		page.type(page.passwordInput, "SuperSecretPassword!");
		page.waitForTimeout(1000);

		page.click(page.loginButton);
		page.waitForTimeout(1000);

		Assert.assertTrue(page.getFlashMessage().contains("secure area"));
		page.waitForTimeout(1000);
	}

	@Test
	public void testDropdownSelection() {
		page.navigateTo("https://the-internet.herokuapp.com/");
		page.waitForTimeout(1000);

		page.goToDropdown();
		page.waitForTimeout(1000);

		page.selectDropdownByValue("2");
		page.waitForTimeout(1000);

		Assert.assertEquals(page.getSelectedDropdownValue(), "2");
		page.waitForTimeout(1000);
	}

	@Test
	public void testJSAlert() {
		page.navigateTo("https://the-internet.herokuapp.com/");
		page.waitForTimeout(1000);

		page.goToJavaScriptAlerts();
		page.waitForTimeout(1000);

		page.clickJsAlert();
		page.waitForTimeout(1000);

		// handle alert
		page.handleAlert(true, null);
		page.waitForTimeout(1000);

		Assert.assertTrue(page.getJsResultText().contains("You successfully"));
	}

	@Test
	public void testFileUpload() {
		page.navigateTo("https://the-internet.herokuapp.com/");
		page.waitForTimeout(1000);

		page.goToFileUpload();
		page.waitForTimeout(1000);

		String filePath = "src/test/resources/testdata/Login.json";
		page.uploadFile(filePath);
		page.waitForTimeout(1000);

		Assert.assertTrue(page.getUploadedFileName().contains("Login.json"));
	}

	@Test
	public void testNestedFrames() {

		page.navigateTo("https://the-internet.herokuapp.com/nested_frames");
		page.waitForTimeout(1000);

		// -------------------------
		// TOP -> MIDDLE
		// -------------------------
		Frame topFrame = page.switchToFrame("frame-top");
		Frame middleFrame = topFrame.childFrames().stream().filter(f -> "frame-middle".equals(f.name())).findFirst()
				.orElseThrow(() -> new RuntimeException("Middle frame not found"));

		String middleText = page.getTextFromFrame(middleFrame, "body");
		System.out.println("Middle Frame Text: " + middleText);

		page.switchToDefaultFrame();
		page.waitForTimeout(1000);

		// -------------------------
		// TOP -> LEFT
		// -------------------------
		topFrame = page.switchToFrame("frame-top");
		Frame leftFrame = topFrame.childFrames().stream().filter(f -> "frame-left".equals(f.name())).findFirst()
				.orElseThrow(() -> new RuntimeException("Left frame not found"));

		String leftText = page.getTextFromFrame(leftFrame, "body");
		System.out.println("Left Frame Text: " + leftText);

		page.switchToDefaultFrame();
		page.waitForTimeout(1000);

		// -------------------------
		// TOP -> RIGHT
		// -------------------------
		topFrame = page.switchToFrame("frame-top");
		Frame rightFrame = topFrame.childFrames().stream().filter(f -> "frame-right".equals(f.name())).findFirst()
				.orElseThrow(() -> new RuntimeException("Right frame not found"));

		String rightText = page.getTextFromFrame(rightFrame, "body");
		System.out.println("Right Frame Text: " + rightText);

		page.switchToDefaultFrame();
		page.waitForTimeout(1000);

		// -------------------------
		// BOTTOM
		// -------------------------
		Frame bottomFrame = page.switchToFrame("frame-bottom");
		String bottomText = page.getTextFromFrame(bottomFrame, "body");
		System.out.println("Bottom Frame Text: " + bottomText);

		page.switchToDefaultFrame();

		Assert.assertTrue(middleText.contains("MIDDLE"));
		Assert.assertTrue(leftText.contains("LEFT"));
		Assert.assertTrue(rightText.contains("RIGHT"));
		Assert.assertTrue(bottomText.contains("BOTTOM"));
	}

	@Test
	public void testBrowserBackForward() {

		page.navigateTo("https://the-internet.herokuapp.com/");
		page.waitForTimeout(1000);

		// Go to a new page
		page.navigateTo("https://the-internet.herokuapp.com/dynamic_loading");
		page.waitForTimeout(1000);

		// Go back to home
		page.browserBack();
		page.waitForTimeout(1000);
		Assert.assertTrue(page.getCurrentUrl().contains("the-internet.herokuapp.com/"));

		// Go forward
		page.browserForward();
		page.waitForTimeout(1000);
		Assert.assertTrue(page.getCurrentUrl().contains("dynamic_loading"));
	}

}
