package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminEventReviewPage extends BasePage {

    private final By eventName = By.tagName("h1");
    private final By btnApprove = By.xpath("//button[contains(text(), 'Duyệt sự kiện')]");
    private final By btnReject = By.xpath("//button[contains(text(), 'Từ chối')]");
    private final By rejectReasonTextarea = By.cssSelector("textarea[name='reason']");
    private final By btnConfirmReject = By.xpath("//button[contains(., 'Xác nhận từ chối')]");
    private final By btnCancelDialog = By.xpath("//button[contains(text(), 'Hủy bỏ')]");
    private final By errorAlert = By.cssSelector("p[role='alert']");
    private final By approveDialog = By.xpath("//h2[contains(text(), 'Xác nhận duyệt sự kiện')]");
    private final By ticketRows = By.cssSelector("table tbody tr");

    public AdminEventReviewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(eventName);
    }

    public String getEventName() {
        return getText(eventName);
    }

    public void clickReject() {
        click(btnReject);
        waitForVisibility(rejectReasonTextarea);
    }

    public boolean isRejectDialogDisplayed() {
        return isDisplayed(rejectReasonTextarea);
    }

    public void enterRejectReason(String reason) {
        type(rejectReasonTextarea, reason);
    }

    public void clickConfirmReject() {
        click(btnConfirmReject);
    }

    public void clickCancelDialog() {
        click(btnCancelDialog);
    }

    public boolean isErrorAlertDisplayed() {
        return isDisplayed(errorAlert);
    }

    public void clickApprove() {
        click(btnApprove);
        waitForVisibility(approveDialog);
    }

    public boolean isConfirmApproveDialogDisplayed() {
        return isDisplayed(approveDialog);
    }

    public int getTicketTypesCount() {
        return driver.findElements(ticketRows).size();
    }

    public void clickBack() {
        By backLink = By.cssSelector("a[href='/admin/events'], a[href*='pending']");
        if (isDisplayed(backLink)) {
            click(backLink);
        } else {
            driver.navigate().back();
            delay();
        }
    }
}

