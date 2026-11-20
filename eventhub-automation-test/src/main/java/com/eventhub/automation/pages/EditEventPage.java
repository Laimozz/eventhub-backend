package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EditEventPage extends BasePage {

    private final By nameInput = By.id("name");
    private final By descriptionInput = By.id("description");
    private final By cityInput = By.id("city");
    private final By addressInput = By.id("address");
    private final By capacityInput = By.id("capacity");
    private final By btnNextStep = By.xpath("//button[contains(., 'Tiếp tục: Bước 2') or contains(., 'Tiếp tục')]");
    private final By btnSave = By.xpath("//button[contains(., 'Lưu bản nháp') or contains(., 'Lưu thay đổi') or contains(., 'Cập nhật')]");
    private final By errorBanner = By.cssSelector("p[role='alert'], p[class*='errorBanner']");
    private final By statusNotice = By.cssSelector("p[role='status'], p[class*='notice']");

    public EditEventPage(WebDriver driver) {
        super(driver);
    }

    public EditEventPage openEventEdit(long eventId) {
        if (eventId <= 1) {
            open(ConfigurationManager.getBaseUrl() + "/organizer/events");
            java.util.List<WebElement> editLinks = driver.findElements(By.cssSelector("a[href*='/edit/details']"));
            if (!editLinks.isEmpty()) {
                String href = editLinks.get(0).getAttribute("href");
                open(href);
                return this;
            }
            eventId = 29;
        }
        open(ConfigurationManager.getBaseUrl() + "/organizer/events/" + eventId + "/edit/details");
        return this;
    }

    public String getEventNameValue() {
        WebElement element = waitForVisibility(nameInput);
        highlightElement(element);
        return element.getAttribute("value");
    }

    public void updateEventName(String newName) {
        type(nameInput, newName);
    }

    public void updateDescription(String newDescription) {
        type(descriptionInput, newDescription);
    }

    public void updateAddress(String newAddress) {
        type(addressInput, newAddress);
    }

    public void clickNextStep() {
        click(btnNextStep);
    }

    public void clickSave() {
        click(btnSave);
    }

    public boolean isErrorBannerDisplayed() {
        return isDisplayed(errorBanner);
    }

    public String getErrorBannerText() {
        return getText(errorBanner);
    }

    public boolean isStatusNoticeDisplayed() {
        return isDisplayed(statusNotice);
    }
}

