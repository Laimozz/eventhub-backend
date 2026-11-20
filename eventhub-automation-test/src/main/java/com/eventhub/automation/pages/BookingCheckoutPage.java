package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BookingCheckoutPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Xác nhận & thanh toán')]");
    private final By bookingCode = By.xpath("//div[contains(@class, 'row')]//strong[contains(text(), 'DH-') or contains(text(), '#')]");
    private final By totalAmount = By.cssSelector("div[class*='total'] strong");
    private final By termsCheckbox = By.cssSelector("input[type='checkbox']");
    private final By btnPay = By.xpath("//button[contains(., 'Thanh toán qua VNPay') or contains(., 'Đang chuyển')]");
    private final By countdownTimer = By.cssSelector("span[class*='timer'], div[class*='holdNotice']");

    public BookingCheckoutPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public boolean isCountdownTimerDisplayed() {
        return isDisplayed(countdownTimer);
    }

    public String getTotalAmount() {
        return getText(totalAmount);
    }

    public boolean isTermsChecked() {
        WebElement cb = waitForVisibility(termsCheckbox);
        highlightElement(cb);
        return cb.isSelected();
    }

    public void setTerms(boolean accept) {
        WebElement cb = waitForVisibility(termsCheckbox);
        highlightElement(cb);
        if (cb.isSelected() != accept) {
            cb.click();
            delay();
        }
    }

    public boolean isPayButtonEnabled() {
        try {
            WebElement btn = driver.findElement(btnPay);
            highlightElement(btn);
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickPay() {
        click(btnPay);
    }

    public void clickBackToHistory() {
        By backLink = By.xpath("//a[contains(text(), 'Lịch sử booking')]");
        if (isDisplayed(backLink)) {
            click(backLink);
        } else {
            driver.navigate().back();
            delay();
        }
    }
}

