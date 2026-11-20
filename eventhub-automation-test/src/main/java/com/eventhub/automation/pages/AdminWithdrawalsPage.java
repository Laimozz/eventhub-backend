package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminWithdrawalsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Yêu cầu rút tiền')]");
    private final By statusSelect = By.tagName("select");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By detailLinks = By.xpath("//table//a[contains(text(), 'Xem #')]");
    private final By paginationText = By.xpath("//div[contains(@class, 'pagination')]//span");
    private final By nextButton = By.xpath("//div[contains(@class, 'pagination')]//button[contains(text(), 'Sau')]");
    private final By prevButton = By.xpath("//div[contains(@class, 'pagination')]//button[contains(text(), 'Trước')]");
    private final By statusBadges = By.xpath("//table tbody tr td span[contains(@class, 'badge')]");
    private final By emptyMessage = By.cssSelector("p[class*='empty']");

    public AdminWithdrawalsPage(WebDriver driver) {
        super(driver);
    }

    public AdminWithdrawalsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/admin/withdrawals");
        waitForVisibility(pageTitle);
        return this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public void filterStatus(String status) {
        WebElement selectElem = waitForVisibility(statusSelect);
        highlightElement(selectElem);
        selectElem.sendKeys(status);
        delay();
    }

    public int getWithdrawalsCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public boolean hasWithdrawalRows() {
        return getWithdrawalsCount() > 0;
    }

    public void clickFirstWithdrawalDetail() {
        List<WebElement> links = driver.findElements(detailLinks);
        if (!links.isEmpty()) {
            WebElement firstLink = links.get(0);
            highlightElement(firstLink);
            firstLink.click();
            delay();
        }
    }

    public boolean isPaginationDisplayed() {
        return isDisplayed(paginationText);
    }

    public String getPaginationText() {
        return getText(paginationText);
    }

    public void clickNextPage() {
        click(nextButton);
    }

    public void clickPreviousPage() {
        click(prevButton);
    }

    public String getFirstRowStatusBadgeText() {
        List<WebElement> badges = driver.findElements(statusBadges);
        if (!badges.isEmpty()) {
            return badges.get(0).getText().trim();
        }
        return "";
    }

    public boolean hasEmptyMessage() {
        return isDisplayed(emptyMessage);
    }
}
