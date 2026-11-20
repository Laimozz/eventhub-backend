package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class StaffDispatchPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Phân công nhân viên') or contains(text(), 'Điều phối') or contains(text(), 'sự kiện')]");
    private final By searchInput = By.cssSelector("input[placeholder*='Tìm theo tên']");
    private final By filterButtons = By.cssSelector("button[class*='filter']");
    private final By eventCards = By.cssSelector("article[class*='eventCard'], div[class*='eventCard']");

    public StaffDispatchPage(WebDriver driver) {
        super(driver);
    }

    public StaffDispatchPage open() {
        open(ConfigurationManager.getBaseUrl() + "/organizer/staffs");
        delay();
        return this;
    }

    public void searchEvent(String keyword) {
        type(searchInput, keyword);
        delay();
    }

    public int getEventsCount() {
        List<WebElement> cards = driver.findElements(eventCards);
        return cards.size();
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public void filterByStatus(String statusLabel) {
        filterStatus(statusLabel);
    }

    public void filterStatus(String statusLabel) {
        By statusBtn = By.xpath("//button[contains(., '" + statusLabel + "')]");
        if (isDisplayed(statusBtn)) {
            click(statusBtn);
        }
    }
}

