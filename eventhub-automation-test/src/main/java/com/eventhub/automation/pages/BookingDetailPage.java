package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BookingDetailPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Chi tiết booking')]");
    private final By totalAmount = By.cssSelector("div[class*='total'] strong");
    private final By statusBadge = By.cssSelector("span[class*='statusBadge'], span[class*='badge']");
    private final By qrButton = By.xpath("//a[contains(., 'Xem vé điện tử / QR') or contains(., 'Xem vé QR')]");
    private final By linkBackToEvent = By.xpath("//a[contains(text(), 'Xem sự kiện')]");

    public BookingDetailPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public String getBookingTitle() {
        return getText(pageTitle);
    }

    public String getTotalAmount() {
        return getText(totalAmount);
    }

    public boolean isQrButtonAvailable() {
        return isDisplayed(qrButton);
    }

    public void clickBackToEvent() {
        click(linkBackToEvent);
    }
}

