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

public class LoginTC_Csv extends TestBase {

	InternetHerokuAppPage page;

	@BeforeClass
	public void setup() {
		TestBase.setBrowser("chrome");
		TestBase.setHeadless(true);
		TestBase.setUrl("https://the-internet.herokuapp.com/");
		page = new InternetHerokuAppPage();
	}

	@Test(dataProvider = "testData", dataProviderClass = DynamicDataProvider.class, description = "Login test for Herokuapp")
	@TestDataFile(file = "src/test/resources/testdata/Login.csv")
	public void testLoginWithCsv(Map<String, String> data) {
		String username = data.get("username");
		String password = data.get("password");

		page.goToLogin();
		page.login(username, password);
		page.waitForTimeout(2000);

		String message = page.getFlashMessage();
		Assert.assertTrue(message.contains("You logged into a secure area!"));

		ExtentLogger.pass("Login validated successfully");
		page.captureFullPageScreenshot("Login_Success");
	}

}
