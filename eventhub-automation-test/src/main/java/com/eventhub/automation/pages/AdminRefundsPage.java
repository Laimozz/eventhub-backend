package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminRefundsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Sự kiện đã hủy')]");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By detailLinks = By.xpath("//table//a[contains(text(), 'Xem giao dịch')]");

    public AdminRefundsPage(WebDriver driver) {
        super(driver);
    }

    public AdminRefundsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/admin/refunds");
        waitForVisibility(pageTitle);
        return this;
    }

    public int getCanceledEventsCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public int getRefundEventsCount() {
        return getCanceledEventsCount();
    }

    public void clickFirstEventRefundDetail() {
        List<WebElement> links = driver.findElements(detailLinks);
        if (!links.isEmpty()) {
            WebElement firstLink = links.get(0);
            highlightElement(firstLink);
            firstLink.click();
            delay();
        }
    }
}

