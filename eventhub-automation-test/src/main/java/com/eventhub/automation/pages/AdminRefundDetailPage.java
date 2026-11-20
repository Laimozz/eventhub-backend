package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminRefundDetailPage extends BasePage {

    private final By headingTitle = By.tagName("h1");
    private final By totalRefundAmount = By.xpath("//div[contains(text(), 'TỔNG CẦN HOÀN TRẢ')]/following-sibling::div[1]");
    private final By transactionsHeading = By.xpath("//h2[contains(text(), 'Giao dịch và lịch sử hoàn tiền')]");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By btnBatchRefund = By.xpath("//button[contains(text(), 'Hoàn tiền hàng loạt')]");
    private final By emptyTransactionsMsg = By.xpath("//p[contains(text(), 'Không có giao dịch thanh toán') or contains(text(), 'Chưa có hồ sơ hoàn tiền')]");

    public AdminRefundDetailPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(headingTitle);
    }

    public String getHeadingTitle() {
        return getText(headingTitle);
    }

    public boolean isReconciliationTileDisplayed() {
        return driver.findElements(By.xpath("//div[contains(text(), 'TỔNG CẦN HOÀN TRẢ') or contains(text(), 'ĐÃ HOÀN TIỀN')]")).size() > 0;
    }

    public boolean isTransactionsSectionDisplayed() {
        return isDisplayed(transactionsHeading);
    }

    public int getPaymentsRowCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public boolean hasTransactionsOrEmptyState() {
        return getPaymentsRowCount() > 0 || isDisplayed(emptyTransactionsMsg);
    }

    public boolean isBatchRefundButtonPresent() {
        return !driver.findElements(btnBatchRefund).isEmpty();
    }

    public void clickBack() {
        By backLink = By.cssSelector("a[href='/admin/refunds']");
        if (isDisplayed(backLink)) {
            click(backLink);
        } else {
            driver.navigate().back();
            delay();
        }
    }
}
