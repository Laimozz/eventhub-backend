package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProfilePage extends BasePage {

    // View mode locators
    private final By headlineTitle = By.xpath("//h1[contains(text(), 'Thông tin cá nhân') or contains(text(), 'Chỉnh sửa hồ sơ')]");
    private final By btnEditProfile = By.xpath("//button[contains(., 'Chỉnh sửa thông tin cá nhân')]");
    private final By heroFullName = By.cssSelector("h2[class*='heroName']");
    private final By emailText = By.cssSelector("div[class*='quickInfoItem'] span:last-child");

    // Edit mode locators
    private final By fullNameInput = By.id("fullName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By birthDateInput = By.id("birthDate");
    private final By roleInput = By.id("userRole");
    private final By btnSubmit = By.cssSelector("button[type='submit']");
    private final By btnCancel = By.cssSelector("button[class*='btnCancel']");
    private final By fieldErrorTexts = By.cssSelector("p[class*='fieldErrorText']");
    private final By toastMessage = By.cssSelector("div[class*='toast'], div[role='status'], [role='alert']");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public ProfilePage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/profile");
        waitForVisibility(headlineTitle);
        return this;
    }

    public String getHeadlineTitle() {
        return getText(headlineTitle);
    }

    public String getHeroFullName() {
        return getText(heroFullName);
    }

    public void clickEditProfile() {
        click(btnEditProfile);
        waitForVisibility(fullNameInput);
    }

    public void enterFullName(String name) {
        type(fullNameInput, name);
    }

    public void enterPhone(String phone) {
        type(phoneInput, phone);
    }

    public void enterBirthDate(String dateIso) {
        WebElement elem = waitForVisibility(birthDateInput);
        highlightElement(elem);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var el = arguments[0];" +
            "var val = arguments[1];" +
            "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "nativeInputValueSetter.call(el, val);" +
            "el.dispatchEvent(new Event('input', { bubbles: true }));" +
            "el.dispatchEvent(new Event('change', { bubbles: true }));",
            elem, dateIso);
        delay();
    }

    public void clickSubmit() {
        click(btnSubmit);
    }

    public void clickCancel() {
        click(btnCancel);
    }

    public boolean isRoleFieldReadOnly() {
        WebElement roleElem = waitForVisibility(roleInput);
        highlightElement(roleElem);
        delay();
        String disabledAttr = roleElem.getAttribute("disabled");
        String readOnlyAttr = roleElem.getAttribute("readonly");
        return "true".equals(disabledAttr) || "true".equals(readOnlyAttr) || roleElem.getAttribute("class").contains("readonly");
    }

    public String getFirstFieldError() {
        return getText(fieldErrorTexts);
    }

    public boolean isToastOrSuccessDisplayed() {
        return isDisplayed(toastMessage) || isDisplayed(heroFullName);
    }
}

