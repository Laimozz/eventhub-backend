package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminWithdrawalDetailPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Yêu cầu #')]");
    private final By btnApprove = By.xpath("//button[contains(text(), 'Duyệt')]");
    private final By btnReject = By.xpath("//button[contains(text(), 'Từ chối')]");
    private final By dialogTextarea = By.tagName("textarea");
    private final By btnConfirmDialog = By.xpath("//dialog//button[contains(., 'Xác nhận')] | //button[contains(., 'Xác nhận')]");
    private final By btnCancelDialog = By.xpath("//button[contains(text(), 'Hủy bỏ')]");
    private final By noticeStatus = By.cssSelector("p[role='status']");
    private final By errorAlert = By.cssSelector("p[role='alert']");

    public AdminWithdrawalDetailPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public boolean hasActionButtons() {
        return !driver.findElements(btnReject).isEmpty();
    }

    public void clickReject() {
        if (hasActionButtons()) {
            click(btnReject);
            waitForVisibility(dialogTextarea);
        }
    }

    public boolean isRejectDialogDisplayed() {
        return isDisplayed(dialogTextarea);
    }

    public void clickApprove() {
        if (hasActionButtons()) {
            click(btnApprove);
            waitForVisibility(dialogTextarea);
        }
    }

    public boolean isApproveDialogDisplayed() {
        return isDisplayed(dialogTextarea);
    }

    public void enterReasonOrCode(String text) {
        type(dialogTextarea, text);
    }

    public void clickConfirmDialog() {
        click(btnConfirmDialog);
    }

    public void clickCancelDialog() {
        click(btnCancelDialog);
    }

    public boolean isSuccessNoticeDisplayed() {
        return isDisplayed(noticeStatus);
    }

    public String getNoticeText() {
        return getText(noticeStatus);
    }

    public boolean isErrorAlertDisplayed() {
        return isDisplayed(errorAlert);
    }

    public void clickBack() {
        By backLink = By.cssSelector("a[href='/admin/withdrawals']");
        if (isDisplayed(backLink)) {
            click(backLink);
        } else {
            driver.navigate().back();
            delay();
        }
    }
}
