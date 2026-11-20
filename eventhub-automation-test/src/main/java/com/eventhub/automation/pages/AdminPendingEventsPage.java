package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminPendingEventsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Sự kiện chờ phê duyệt')]");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By eventLinks = By.cssSelector("table tbody tr td:first-child a");
    private final By emptyMessage = By.cssSelector("p[class*='empty']");
    private final By noticeStatus = By.cssSelector("p[role='status']");

    public AdminPendingEventsPage(WebDriver driver) {
        super(driver);
    }

    public AdminPendingEventsPage open() {
        super.open(ConfigurationManager.getBaseUrl() + "/admin/events/pending");
        waitForVisibility(pageTitle);
        return this;
    }

    public AdminPendingEventsPage openWithQuery(String queryParams) {
        super.open(ConfigurationManager.getBaseUrl() + "/admin/events/pending" + queryParams);
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public int getPendingEventsCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public void clickFirstPendingEvent() {
        List<WebElement> links = driver.findElements(eventLinks);
        if (!links.isEmpty()) {
            WebElement firstLink = links.get(0);
            highlightElement(firstLink);
            firstLink.click();
            delay();
        }
    }

    public boolean hasEmptyMessage() {
        return isDisplayed(emptyMessage);
    }

    public String getEmptyMessageText() {
        return getText(emptyMessage);
    }

    public boolean isNoticeDisplayed() {
        return isDisplayed(noticeStatus);
    }

    public String getNoticeText() {
        return getText(noticeStatus);
    }
}
