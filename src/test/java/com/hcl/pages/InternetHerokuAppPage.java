package com.hcl.pages;

import com.hcl.base.PageBase;
import com.microsoft.playwright.Page;

public class InternetHerokuAppPage extends PageBase {

    // -------------------- Locators --------------------
    public final String loginLink = "a[href='/login']";
    public final String usernameInput = "#username";
    public final String passwordInput = "#password";
    public final String loginButton = "button[type='submit']";
    public final String flashMessage = "#flash";

    public final String dropdownLink = "a[href='/dropdown']";
    public final String dropdownSelect = "#dropdown";

    public final String javascriptLink = "a[href='/javascript_alerts']";
    public final String jsAlertButton = "button[onclick='jsAlert()']";
    public final String jsResultText = "#result";

    public final String fileUploadLink = "a[href='/upload']";
    public final String fileUploadInput = "#file-upload";
    public final String fileUploadButton = "#file-submit";
    public final String fileUploadResult = "#uploaded-files";

    public final String iframeLink = "a[href='/iframe']";
    public final String iframe = "#mce_0_ifr";
    public final String iframeBody = "body";

    // -------------------- Constructor --------------------
    public InternetHerokuAppPage() {
        super();
    }

    // -------------------- Navigation --------------------
    public void goToLogin() {
        click(loginLink);
    }

    public void goToDropdown() {
        click(dropdownLink);
    }

    public void goToJavaScriptAlerts() {
        click(javascriptLink);
    }

    public void goToFileUpload() {
        click(fileUploadLink);
    }

    public void goToIframePage() {
    	navigateTo("https://the-internet.herokuapp.com/frames");
        click(iframeLink);
    }

    // -------------------- Login Actions --------------------
    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginButton);
    }

    public String getFlashMessage() {
        return getText(flashMessage);
    }

    // -------------------- Dropdown Actions --------------------
    public void selectDropdownByValue(String value) {
        selectByValue(dropdownSelect, value);
    }

    public String getSelectedDropdownValue() {
        return getSelectedDropdownValue(dropdownSelect);
    }

    // -------------------- JS Alert Actions --------------------
    public void clickJsAlert() {
        click(jsAlertButton);
    }

    public String getJsResultText() {
        return getText(jsResultText);
    }

    // -------------------- File Upload Actions --------------------
    public void uploadFile(String filePath) {
        uploadFile(fileUploadInput, filePath);
        click(fileUploadButton);
    }

    public String getUploadedFileName() {
        return getText(fileUploadResult);
    }

    // -------------------- iFrame Actions --------------------
    public void switchToIframe() {
        switchToFrame(iframe);
    }

    public void switchToDefault() {
    	switchToDefaultFrame();
    }

    public void typeInsideIframe(String text) {
        getPage().frameLocator(iframe).locator(iframeBody).fill(text);
    }

    public String getTextInsideIframe() {
        return getPage().frameLocator(iframe).locator(iframeBody).innerText();
    }
}
