package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By alertMessage = By.cssSelector("[role='alert']");
    private final By successNotice = By.cssSelector("[role='status']");
    private final By registerLink = By.cssSelector("a[href='/register']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        open(ConfigurationManager.getBaseUrl() + "/login");
        return this;
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPassword(String password) {
        type(passwordInput, password);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSubmit();
        try {
            wait.until(d -> !d.getCurrentUrl().contains("/login") || isErrorMessageDisplayed());
        } catch (Exception ignored) {
        }
    }

    public String getErrorMessage() {
        return getText(alertMessage);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(alertMessage);
    }

    public String getSuccessNotice() {
        return getText(successNotice);
    }

    public boolean isSuccessNoticeDisplayed() {
        return isDisplayed(successNotice);
    }

    public void clickRegisterLink() {
        click(registerLink);
    }
}

