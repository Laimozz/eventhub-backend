package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class NotificationsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Thông báo')]");
    private final By notificationItems = By.cssSelector("article");
    private final By btnMarkAllRead = By.xpath("//button[contains(text(), 'Đánh dấu tất cả đã đọc')]");
    private final By unreadItems = By.cssSelector("article[class*='unread']");

    public NotificationsPage(WebDriver driver) {
        super(driver);
    }

    public NotificationsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/thong-bao");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public int getNotificationsCount() {
        List<WebElement> items = driver.findElements(notificationItems);
        return items.size();
    }

    public boolean isMarkAllReadButtonEnabled() {
        try {
            WebElement btn = driver.findElement(btnMarkAllRead);
            highlightElement(btn);
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickMarkAllRead() {
        if (isMarkAllReadButtonEnabled()) {
            click(btnMarkAllRead);
        }
    }

    public boolean hasUnreadNotification() {
        return !driver.findElements(unreadItems).isEmpty();
    }
}

