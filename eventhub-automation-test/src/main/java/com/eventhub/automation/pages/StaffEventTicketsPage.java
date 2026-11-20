package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class StaffEventTicketsPage extends BasePage {

    private final By eventTitle = By.tagName("h1");
    private final By searchTicketInput = By.cssSelector("input[placeholder*='mã vé'], input[placeholder*='Tìm kiếm']");
    private final By ticketRows = By.cssSelector("table tbody tr, article[class*='ticket'], div[class*='ticketCard']");
    private final By filterCheckedInBtn = By.xpath("//button[contains(., 'Đã check-in')]");
    private final By filterUncheckedInBtn = By.xpath("//button[contains(., 'Chưa check-in')]");
    private final By filterAllBtn = By.xpath("//button[contains(., 'Tất cả')]");
    private final By kpiCheckedIn = By.xpath("//*[contains(text(), 'Đã check-in')]");
    private final By kpiRemaining = By.xpath("//*[contains(text(), 'Chưa check-in')]");

    public StaffEventTicketsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(eventTitle);
    }

    public int getTicketListCount() {
        List<WebElement> rows = driver.findElements(ticketRows);
        return rows.size();
    }

    public void searchTicket(String query) {
        if (isDisplayed(searchTicketInput)) {
            type(searchTicketInput, query);
        }
    }

    public void filterByCheckedIn() {
        click(filterCheckedInBtn);
    }

    public void filterByUncheckedIn() {
        click(filterUncheckedInBtn);
    }

    public void filterByAll() {
        click(filterAllBtn);
    }

    public boolean isKpiDisplayed() {
        return isDisplayed(kpiCheckedIn) && isDisplayed(kpiRemaining);
    }
}

