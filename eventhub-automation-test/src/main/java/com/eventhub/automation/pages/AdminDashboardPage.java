package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminDashboardPage extends BasePage {

    private final By headingTitle = By.xpath("//h1[contains(text(), 'Thống kê tổng quan') or contains(text(), 'Tổng quan')]");
    private final By btnRefresh = By.xpath("//button[contains(text(), 'Làm mới')]");
    private final By metricItems = By.xpath("//*[contains(@class, 'metrics')]//div | //*[contains(@class, 'metricCard')]");
    private final By grossRevenueCard = By.xpath("//*[contains(text(), 'Tiền vé thanh toán thành công')]");
    private final By netRevenueCard = By.xpath("//*[contains(text(), 'Doanh thu nền tảng')]");
    private final By totalEventsCard = By.xpath("//*[contains(text(), 'Tổng số sự kiện')]");
    private final By linkUsers = By.cssSelector("a[href='/admin/users']");
    private final By linkCategories = By.cssSelector("a[href='/admin/categories']");
    private final By linkEvents = By.cssSelector("a[href='/admin/events']");
    private final By linkRefunds = By.cssSelector("a[href='/admin/refunds']");
    private final By linkWithdrawals = By.cssSelector("a[href='/admin/withdrawals']");

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    public AdminDashboardPage open() {
        open(ConfigurationManager.getBaseUrl() + "/admin/statistics");
        return this;
    }

    public boolean isHeadingDisplayed() {
        return isDisplayed(headingTitle);
    }

    public void clickRefresh() {
        click(btnRefresh);
    }

    public boolean isGrossRevenueMetricDisplayed() {
        return isDisplayed(grossRevenueCard);
    }

    public boolean isNetRevenueMetricDisplayed() {
        return isDisplayed(netRevenueCard);
    }

    public boolean isTotalEventsMetricDisplayed() {
        return isDisplayed(totalEventsCard);
    }

    public int getMetricCardsCount() {
        try {
            List<WebElement> list = driver.findElements(metricItems);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void navigateToUsers() {
        click(linkUsers);
    }

    public void navigateToCategories() {
        click(linkCategories);
    }

    public void navigateToEvents() {
        click(linkEvents);
    }

    public void navigateToRefunds() {
        click(linkRefunds);
    }

    public void navigateToWithdrawals() {
        click(linkWithdrawals);
    }
}
