package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ExplorePage extends BasePage {
    private final By searchInput = By.cssSelector("input[placeholder*='Tìm kiếm']");
    private final By eventCards = By.cssSelector("div[class*='grid'] > article, article[class*='card'], article[class*='eventCard'], div[class*='grid'] > a");
    private final By emptyState = By.xpath("//h3[contains(text(), 'Không tìm thấy sự kiện nào')]");
    private final By applyFilterButton = By.xpath("//button[contains(., 'Áp dụng bộ lọc')]");
    private final By resetFilterButton = By.xpath("//button[contains(., 'Xóa bộ lọc')]");

    // Bộ lọc nhanh thời gian
    private final By timeTodayRadio = By.xpath("//label[contains(., 'Hôm nay')] | //span[contains(text(), 'Hôm nay')]");
    private final By timeThisWeekRadio = By.xpath("//label[contains(., 'Tuần này')] | //span[contains(text(), 'Tuần này')]");
    private final By timeThisMonthRadio = By.xpath("//label[contains(., 'Tháng này')] | //span[contains(text(), 'Tháng này')]");

    // Bộ lọc thành phố
    private final By citySelect = By.cssSelector("select[class*='select'], select");

    public ExplorePage(WebDriver driver) {
        super(driver);
    }

    public ExplorePage open() {
        open(ConfigurationManager.getBaseUrl() + "/customer/explore");
        return this;
    }

    public void search(String keyword) {
        WebElement input = waitForVisibility(searchInput);
        highlightElement(input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.clear();
        if (keyword != null && !keyword.isEmpty()) {
            input.sendKeys(keyword);
        }
        delay();
        input.sendKeys(Keys.ENTER);
        delay();
    }

    public int getEventCardCount() {
        try {
            List<WebElement> cards = driver.findElements(eventCards);
            return cards.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getEventCardsCount() {
        return getEventCardCount();
    }

    public ExplorePage openWithFilter(String queryString) {
        open(ConfigurationManager.getBaseUrl() + "/customer/explore?" + queryString);
        return this;
    }

    public void selectCity(String city) {
        WebElement selectElem = waitForVisibility(citySelect);
        highlightElement(selectElem);
        selectElem.sendKeys(city);
        delay();
        clickApplyFilter();
    }

    public boolean isEmptyStateDisplayed() {
        return isDisplayed(emptyState);
    }

    public void clickFirstEventCard() {
        waitForVisibility(eventCards);
        WebElement firstCard = driver.findElements(eventCards).getFirst();
        highlightElement(firstCard);
        delay();
        try {
            WebElement link = firstCard.findElement(By.tagName("a"));
            link.click();
        } catch (Exception e) {
            firstCard.click();
        }
        delay();
    }

    public void filterByQuickTime(String time) {
        if ("today".equalsIgnoreCase(time)) {
            click(timeTodayRadio);
        } else if ("this_week".equalsIgnoreCase(time)) {
            click(timeThisWeekRadio);
        } else if ("this_month".equalsIgnoreCase(time)) {
            click(timeThisMonthRadio);
        }
        clickApplyFilter();
    }

    public void clickApplyFilter() {
        if (isDisplayed(applyFilterButton)) {
            click(applyFilterButton);
        }
    }

    public void clickResetFilter() {
        if (isDisplayed(resetFilterButton)) {
            click(resetFilterButton);
        }
    }
}

