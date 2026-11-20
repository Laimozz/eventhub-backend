package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class BookingHistoryPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Lịch sử đặt vé') or contains(text(), 'Booking của tôi')]");
    private final By bookingCards = By.cssSelector("article[class*='bookingCard']");
    private final By detailLinks = By.xpath("//a[contains(text(), 'Xem chi tiết booking')]");

    public BookingHistoryPage(WebDriver driver) {
        super(driver);
    }

    public BookingHistoryPage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/booking-cua-toi");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public int getBookingCount() {
        List<WebElement> cards = driver.findElements(bookingCards);
        return cards.size();
    }

    public void clickFirstBookingDetail() {
        List<WebElement> links = driver.findElements(detailLinks);
        if (!links.isEmpty()) {
            WebElement firstLink = links.get(0);
            highlightElement(firstLink);
            firstLink.click();
            delay();
        }
    }
}

