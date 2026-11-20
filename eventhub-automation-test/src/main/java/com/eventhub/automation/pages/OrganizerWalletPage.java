package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OrganizerWalletPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Ví') or contains(text(), 'Doanh thu') or contains(text(), 'Tài chính')]");
    private final By btnOpenWithdrawModal = By.cssSelector("button[class*='btnCreateWithdrawal'], button[class*='btnQuickWithdraw']");
    private final By amountInput = By.cssSelector("input[type='number']");
    private final By bankSelect = By.tagName("select");
    private final By accountInput = By.cssSelector("input[placeholder*='ví dụ: 007'], input[placeholder*='1234567']");
    private final By btnModalSubmit = By.cssSelector("button[class*='btnModalSubmit']");
    private final By btnModalCancel = By.cssSelector("button[class*='btnModalCancel']");
    private final By modalError = By.xpath("//div[contains(@class, 'modalCard')]//div[contains(@style, 'color: rgb(186, 26, 26)') or contains(@style, '#ba1a1a')]");
    private final By balanceDisplay = By.xpath("//div[contains(@style, 'font-size: 20px') or contains(@class, 'balance')]");

    public OrganizerWalletPage(WebDriver driver) {
        super(driver);
    }

    public OrganizerWalletPage open() {
        open(ConfigurationManager.getBaseUrl() + "/organizer/wallet");
        delay();
        return this;
    }

    public void clickOpenWithdrawModal() {
        click(btnOpenWithdrawModal);
        waitForVisibility(amountInput);
    }

    public void enterWithdrawDetails(String amount, String bank, String accountNumber) {
        type(amountInput, amount);
        if (bank != null && !bank.isEmpty()) {
            WebElement selectElem = waitForVisibility(bankSelect);
            highlightElement(selectElem);
            selectElem.sendKeys(bank);
            delay();
        }
        type(accountInput, accountNumber);
    }

    public void enterAmount(String amount) {
        type(amountInput, amount);
    }

    public void clickProceedConfirm() {
        click(btnModalSubmit);
    }

    public void clickCancelModal() {
        click(btnModalCancel);
    }

    public boolean isLoaded() {
        return isDisplayed(pageTitle);
    }

    public String getAvailableBalance() {
        if (isDisplayed(balanceDisplay)) {
            return getText(balanceDisplay);
        }
        return "0";
    }

    public boolean isModalOpen() {
        return isDisplayed(amountInput);
    }
}
