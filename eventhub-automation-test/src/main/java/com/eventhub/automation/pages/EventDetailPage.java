package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EventDetailPage extends BasePage {

    private final By eventTitle = By.tagName("h1");
    private final By buyButton = By.cssSelector("button[class*='buyButton']");

    public EventDetailPage(WebDriver driver) {
        super(driver);
    }

    public EventDetailPage open(int eventId) {
        open(ConfigurationManager.getBaseUrl() + "/customer/events/" + eventId);
        waitForVisibility(eventTitle);
        return this;
    }

    public EventDetailPage openEvent(int eventId) {
        return open(eventId);
    }

    public String getEventTitle() {
        return getText(eventTitle);
    }

    public boolean isBuyButtonEnabled() {
        return driver.findElement(buyButton).isEnabled();
    }

    public void clickBuyTicket() {
        scrollToElement(buyButton);
        click(buyButton);
    }

    public void clickBuyTickets() {
        clickBuyTicket();
    }
}

