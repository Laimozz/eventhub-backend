package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class OrganizerReportsPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Báo cáo') or contains(text(), 'Thống kê')]");
    private final By eventSelect = By.xpath("//select[contains(@class, 'eventSelect') or @aria-label='Chọn sự kiện lọc']");
    private final By btnRefresh = By.xpath("//button[contains(., 'Làm mới') or contains(@title, 'Tải lại')]");
    private final By btnExportCsv = By.xpath("//button[contains(., 'Xuất file CSV') or contains(@title, 'CSV')]");
    private final By timeRangeBtns = By.xpath("//button[contains(@class, 'timeRangeBtn')]");
    private final By metricCards = By.xpath("//*[contains(@class, 'metricCard') or contains(@class, 'statCard') or contains(@class, 'card')]");
    private final By revenueMetricTitle = By.xpath("//*[contains(@class, 'kpiCard')]//*[contains(., 'doanh thu') or contains(., 'Doanh thu') or contains(., 'DOANH THU')] | //*[contains(text(), 'Tổng doanh thu') or contains(text(), 'Doanh thu')]");
    private final By ticketsSoldMetricTitle = By.xpath("//*[contains(@class, 'kpiCard')]//*[contains(., 'Vé đã bán') or contains(., 'VÉ ĐÃ BÁN') or contains(., 'Số vé')] | //*[contains(text(), 'Vé đã bán') or contains(text(), 'Số vé')]");

    public OrganizerReportsPage(WebDriver driver) {
        super(driver);
    }

    public OrganizerReportsPage open() {
        open(ConfigurationManager.getBaseUrl() + "/organizer/reports");
        return this;
    }

    public boolean isPageTitleDisplayed() {
        return isDisplayed(pageTitle);
    }

    public void selectEvent(int index) {
        WebElement selectElement = waitForVisibility(eventSelect);
        highlightElement(selectElement);
        Select dropdown = new Select(selectElement);
        List<WebElement> options = dropdown.getOptions();
        if (options.size() > index) {
            dropdown.selectByIndex(index);
            delay();
        }
    }

    public void selectEventByVisibleText(String text) {
        WebElement selectElement = waitForVisibility(eventSelect);
        highlightElement(selectElement);
        Select dropdown = new Select(selectElement);
        dropdown.selectByVisibleText(text);
        delay();
    }

    public int getEventFilterOptionCount() {
        try {
            WebElement selectElement = waitForVisibility(eventSelect);
            Select dropdown = new Select(selectElement);
            return dropdown.getOptions().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickTimeRange(String rangeLabel) {
        By rangeBtn = By.xpath("//button[contains(@class, 'timeRangeBtn') and contains(text(), '" + rangeLabel + "')]");
        click(rangeBtn);
    }

    public void clickRefresh() {
        click(btnRefresh);
    }

    public boolean isExportCsvButtonEnabled() {
        try {
            WebElement btn = waitForVisibility(btnExportCsv);
            highlightElement(btn);
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRevenueMetricDisplayed() {
        return isDisplayed(revenueMetricTitle);
    }

    public boolean isTicketsSoldMetricDisplayed() {
        return isDisplayed(ticketsSoldMetricTitle);
    }

    public int getMetricCardsCount() {
        try {
            List<WebElement> list = driver.findElements(metricCards);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }
}

