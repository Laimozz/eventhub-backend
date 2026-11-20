package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SelectTicketsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Chọn loại vé tham dự')]");
    private final By btnIncreaseList = By.cssSelector("button[aria-label^='Tăng']");
    private final By btnDecreaseList = By.cssSelector("button[aria-label^='Giảm']");
    private final By totalSummary = By.cssSelector("div[class*='total'] strong");
    private final By btnContinue = By.xpath("//button[contains(., 'Tiếp tục')]");
    private final By emptyWarning = By.xpath("//p[contains(text(), 'Bạn chưa chọn vé')]");

    public SelectTicketsPage(WebDriver driver) {
        super(driver);
    }

    public SelectTicketsPage open(int eventId) {
        int targetId = (eventId <= 1) ? 41 : eventId;
        open(ConfigurationManager.getBaseUrl() + "/customer/events/" + targetId + "/book");
        waitForVisibility(pageTitle);
        return this;
    }

    public void increaseFirstTicket(int times) {
        List<WebElement> increaseButtons = driver.findElements(btnIncreaseList);
        if (!increaseButtons.isEmpty()) {
            WebElement btn = increaseButtons.get(0);
            for (int i = 0; i < times; i++) {
                if (!btn.isEnabled()) {
                    break;
                }
                highlightElement(btn);
                btn.click();
                delay();
            }
        }
    }

    public boolean isFirstTicketIncreaseButtonEnabled() {
        List<WebElement> increaseButtons = driver.findElements(btnIncreaseList);
        if (!increaseButtons.isEmpty()) {
            return increaseButtons.get(0).isEnabled();
        }
        return false;
    }

    public boolean isContinueButtonEnabled() {
        try {
            WebElement btn = driver.findElement(btnContinue);
            highlightElement(btn);
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String getTotalSummaryText() {
        return getText(totalSummary);
    }

    public boolean isEmptySelectionWarningDisplayed() {
        return isDisplayed(emptyWarning);
    }

    public void clickContinue() {
        click(btnContinue);
    }
}

