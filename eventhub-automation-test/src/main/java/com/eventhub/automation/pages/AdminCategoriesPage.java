package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminCategoriesPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Quản lý danh mục sự kiện')]");
    private final By btnAddCategory = By.xpath("//button[contains(., 'Thêm danh mục')]");
    private final By nameInput = By.cssSelector("input[name='name']");
    private final By descriptionInput = By.cssSelector("textarea[name='description']");
    private final By btnSave = By.xpath("//button[contains(text(), 'Lưu danh mục')]");
    private final By btnCancel = By.xpath("//button[contains(text(), 'Hủy bỏ')]");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By firstCategoryName = By.cssSelector("table tbody tr td:first-child strong");
    private final By btnEditList = By.xpath("//table//button[contains(text(), 'Sửa')]");
    private final By btnDeleteList = By.xpath("//table//button[contains(text(), 'Xóa')]");
    private final By errorAlert = By.cssSelector("p[role='alert']");
    private final By statusNotice = By.cssSelector("p[role='status']");

    public AdminCategoriesPage(WebDriver driver) {
        super(driver);
    }

    public AdminCategoriesPage open() {
        open(ConfigurationManager.getBaseUrl() + "/admin/event-categories");
        waitForVisibility(pageTitle);
        return this;
    }

    public void clickAddCategory() {
        click(btnAddCategory);
        waitForVisibility(nameInput);
    }

    public void enterCategory(String name, String description) {
        type(nameInput, name);
        type(descriptionInput, description);
    }

    public int getCategoriesCount() {
        return driver.findElements(tableRows).size();
    }

    public void clickSave() {
        click(btnSave);
    }

    public void clickCancel() {
        click(btnCancel);
    }

    public int getCategoryCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public boolean isModalOpen() {
        return isDisplayed(nameInput);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorAlert);
    }

    public boolean isNoticeDisplayed() {
        return isDisplayed(statusNotice);
    }

    public void clickFirstEditCategory() {
        List<WebElement> editBtns = driver.findElements(btnEditList);
        if (!editBtns.isEmpty()) {
            WebElement btn = editBtns.get(0);
            highlightElement(btn);
            btn.click();
            delay();
            waitForVisibility(nameInput);
        }
    }

    public void clickFirstDeleteCategory() {
        List<WebElement> deleteBtns = driver.findElements(btnDeleteList);
        if (!deleteBtns.isEmpty()) {
            WebElement btn = deleteBtns.get(0);
            highlightElement(btn);
            btn.click();
            delay();
        }
    }
}

