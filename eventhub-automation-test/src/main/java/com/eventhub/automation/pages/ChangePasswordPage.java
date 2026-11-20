package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChangePasswordPage extends BasePage {

    // Locators for navigating from Profile page
    private final By btnEditProfile = By.xpath("//button[contains(., 'Chỉnh sửa thông tin cá nhân')]");
    private final By btnChangePassword = By.xpath("//button[contains(., 'Thay đổi') or contains(., 'Đổi mật khẩu')]");

    // Form inputs & elements
    private final By currentPasswordInput = By.id("currentPassword");
    private final By newPasswordInput = By.id("newPassword");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By btnSubmit = By.cssSelector("button[type='submit']");
    private final By btnCancel = By.cssSelector("button[class*='btnCancel']");
    private final By fieldErrorTexts = By.cssSelector("p[class*='fieldErrorText']");
    private final By toastContainer = By.cssSelector("div[class*='toastContainer']");
    private final By toastTitle = By.cssSelector("h4[class*='toastTitle']");
    private final By toastBody = By.cssSelector("p[class*='toastMessage']");

    public ChangePasswordPage(WebDriver driver) {
        super(driver);
    }

    public ChangePasswordPage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/profile?mode=CHANGE_PASSWORD");
        waitForVisibility(currentPasswordInput);
        return this;
    }

    public void enterCurrentPassword(String password) {
        type(currentPasswordInput, password);
    }

    public void enterNewPassword(String password) {
        type(newPasswordInput, password);
    }

    public void enterConfirmPassword(String password) {
        type(confirmPasswordInput, password);
    }

    public void clickSubmit() {
        click(btnSubmit);
    }

    public void clickCancel() {
        click(btnCancel);
    }

    public void changePassword(String currentPass, String newPass, String confirmPass) {
        if (currentPass != null && !currentPass.isEmpty()) {
            enterCurrentPassword(currentPass);
        }
        if (newPass != null && !newPass.isEmpty()) {
            enterNewPassword(newPass);
        }
        if (confirmPass != null && !confirmPass.isEmpty()) {
            enterConfirmPassword(confirmPass);
        }
        clickSubmit();
    }

    public boolean isToastOrSuccessDisplayed() {
        return waitForToastOrError();
    }

    public boolean waitForToastOrError() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            return shortWait.until(d -> {
                try {
                    WebElement toast = d.findElement(toastContainer);
                    if (toast != null && toast.isDisplayed()) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
                try {
                    WebElement fieldErr = d.findElement(fieldErrorTexts);
                    if (fieldErr != null && fieldErr.isDisplayed()) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public String getToastMessage() {
        try {
            return getText(toastBody);
        } catch (Exception e) {
            return getText(toastContainer);
        }
    }

    public boolean isFieldErrorDisplayed() {
        try {
            WebElement fieldErr = driver.findElement(fieldErrorTexts);
            return fieldErr != null && fieldErr.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFieldError() {
        return getText(fieldErrorTexts);
    }
}

