package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class MyTicketsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Vé điện tử của tôi')]");
    private final By ticketCards = By.cssSelector("article[class*='ticketCard']");
    private final By btnZoomQrList = By.xpath("//button[contains(., 'Phóng to mã QR')]");
    private final By dialogQr = By.cssSelector("dialog[open]");
    private final By btnCloseDialog = By.xpath("//dialog//button[contains(., 'Đóng')]");
    private final By tabValid = By.xpath("//button[contains(text(), 'Vé chưa sử dụng')]");
    private final By tabUsed = By.xpath("//button[contains(text(), 'Đã check-in')]");
    private final By emptyMessage = By.xpath("//h2[contains(text(), 'Chưa có vé trong mục này') or contains(text(), 'chưa có vé')]");

    public MyTicketsPage(WebDriver driver) {
        super(driver);
    }

    public MyTicketsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/ve-cua-toi");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public int getTicketCardsCount() {
        List<WebElement> cards = driver.findElements(ticketCards);
        return cards.size();
    }

    public void clickZoomFirstQr() {
        List<WebElement> zoomBtns = driver.findElements(btnZoomQrList);
        if (!zoomBtns.isEmpty()) {
            WebElement btn = zoomBtns.get(0);
            highlightElement(btn);
            btn.click();
            delay();
        }
    }

    public boolean isQrDialogDisplayed() {
        return isDisplayed(dialogQr);
    }

    public void closeQrDialog() {
        click(btnCloseDialog);
    }

    public void filterValidTickets() {
        click(tabValid);
    }

    public void filterUsedTickets() {
        click(tabUsed);
    }

    public boolean isEmptyMessageDisplayed() {
        return isDisplayed(emptyMessage);
    }
}

