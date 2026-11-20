package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class StaffDashboardPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Giao diện của Staff')]");
    private final By assignedEventCards = By.cssSelector("article[class*='card'], article[class*='eventCard'], div[class*='eventCard']");
    private final By eventDetailLinks = By.xpath("//a[contains(., 'Chi tiết sự kiện') or contains(., 'Soát vé')]");

    public StaffDashboardPage(WebDriver driver) {
        super(driver);
    }

    public StaffDashboardPage open() {
        open(ConfigurationManager.getBaseUrl() + "/staff");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public int getAssignedEventsCount() {
        List<WebElement> cards = driver.findElements(assignedEventCards);
        return cards.size();
    }

    public void clickFirstAssignedEvent() {
        List<WebElement> links = driver.findElements(eventDetailLinks);
        if (!links.isEmpty()) {
            WebElement firstLink = links.get(0);
            highlightElement(firstLink);
            firstLink.click();
            delay();
        }
    }

    public void clickFirstManageTickets() {
        clickFirstAssignedEvent();
    }
}

