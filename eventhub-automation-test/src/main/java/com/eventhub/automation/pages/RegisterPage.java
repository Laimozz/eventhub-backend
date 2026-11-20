package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {
    private final By roleCustomerRadio = By.cssSelector("input[name='role'][value='CUSTOMER']");
    private final By roleOrganizerRadio = By.cssSelector("input[name='role'][value='ORGANIZER']");
    private final By fullNameInput = By.id("fullName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By passwordInput = By.id("password");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By alertMessage = By.cssSelector("[role='alert']");

    private final By fullNameError = By.id("fullName-error");
    private final By passwordError = By.id("password-error");
    private final By confirmPasswordError = By.id("confirmPassword-error");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        open(ConfigurationManager.getBaseUrl() + "/register");
        return this;
    }

    public void selectCustomerRole() {
        click(roleCustomerRadio);
    }

    public void selectOrganizerRole() {
        click(roleOrganizerRadio);
    }

    public void enterFullName(String fullName) {
        type(fullNameInput, fullName);
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPhone(String phone) {
        type(phoneInput, phone);
    }

    public void enterPassword(String password) {
        type(passwordInput, password);
    }

    public void enterConfirmPassword(String confirmPassword) {
        type(confirmPasswordInput, confirmPassword);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void register(String role, String fullName, String email, String phone, String password, String confirmPassword) {
        if ("ORGANIZER".equalsIgnoreCase(role)) {
            selectOrganizerRole();
        } else {
            selectCustomerRole();
        }
        enterFullName(fullName);
        enterEmail(email);
        if (phone != null) {
            enterPhone(phone);
        }
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        clickSubmit();
    }

    public String getAlertError() {
        return getText(alertMessage);
    }

    public boolean isAlertErrorDisplayed() {
        return isDisplayed(alertMessage);
    }

    public String getFullNameError() {
        return getText(fullNameError);
    }

    public String getPasswordError() {
        return getText(passwordError);
    }

    public String getConfirmPasswordError() {
        return getText(confirmPasswordError);
    }
}

