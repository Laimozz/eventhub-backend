package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class OrganizerEventsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Sự kiện của tôi')]");
    private final By searchInput = By.cssSelector("input[name='search']");
    private final By cancelButtons = By.cssSelector("button[class*='cancelButton']");
    private final By cancelReasonTextarea = By.id("cancel-reason");
    private final By btnConfirmCancel = By.xpath("//button[contains(., 'Xác nhận hủy')]");
    private final By btnKeepEvent = By.xpath("//button[contains(., 'Giữ sự kiện')]");
    private final By errorBanner = By.cssSelector("p[role='alert'], p[class*='errorBanner']");
    private final By cancelDialog = By.xpath("//h2[contains(text(), 'Xác nhận hủy sự kiện')]");

    public OrganizerEventsPage(WebDriver driver) {
        super(driver);
    }

    public OrganizerEventsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/organizer/events");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean hasCancelableEvent() {
        return !driver.findElements(cancelButtons).isEmpty();
    }

    public void clickFirstCancelButton() {
        List<WebElement> btns = driver.findElements(cancelButtons);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            highlightElement(btn);
            btn.click();
            delay();
            waitForVisibility(cancelReasonTextarea);
        }
    }

    public boolean isCancelDialogDisplayed() {
        return isDisplayed(cancelDialog);
    }

    public void enterCancelReason(String reason) {
        type(cancelReasonTextarea, reason);
    }

    public void clickConfirmCancel() {
        click(btnConfirmCancel);
    }

    public void clickKeepEvent() {
        click(btnKeepEvent);
    }

    public void dismissCancelDialog() {
        clickKeepEvent();
    }

    public int getEventsCount() {
        return driver.findElements(By.cssSelector("article, div[class*='eventCard']")).size();
    }

    public boolean isErrorBannerDisplayed() {
        return isDisplayed(errorBanner);
    }

    public String getErrorBannerText() {
        return getText(errorBanner);
    }
}
