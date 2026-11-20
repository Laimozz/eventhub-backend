package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminUsersPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Quản lý người dùng')]");
    private final By searchInput = By.cssSelector("input[name='keyword']");
    private final By roleSelect = By.cssSelector("select[name='role']");
    private final By btnSearch = By.cssSelector("form[class*='filters'] button[type='submit']");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By firstRowEmail = By.cssSelector("table tbody tr td:nth-child(2)");
    private final By firstRowRole = By.cssSelector("table tbody tr td:nth-child(4)");
    private final By lockButtons = By.xpath("//table//button[contains(text(), 'Khóa') or contains(text(), 'Mở khóa')]");
    private final By btnOpenCreateModal = By.xpath("//button[contains(., 'Thêm người dùng')]");
    private final By createFullNameInput = By.cssSelector("input[name='fullName']");
    private final By createEmailInput = By.cssSelector("input[name='email']");
    private final By createPhoneInput = By.cssSelector("input[name='phone']");
    private final By createPasswordInput = By.cssSelector("input[name='password']");
    private final By createRoleSelect = By.cssSelector("dialog select[name='role']");
    private final By btnSubmitCreate = By.xpath("//button[contains(text(), 'Tạo người dùng')]");
    private final By btnCancelModal = By.xpath("//button[contains(text(), 'Hủy bỏ')]");
    private final By noticeText = By.cssSelector("p[role='status'], p[class*='notice']");

    public AdminUsersPage(WebDriver driver) {
        super(driver);
    }

    public AdminUsersPage open() {
        open(ConfigurationManager.getBaseUrl() + "/admin/users");
        waitForVisibility(pageTitle);
        return this;
    }

    public void searchUser(String keyword) {
        type(searchInput, keyword);
        click(btnSearch);
    }

    public void filterRole(String role) {
        WebElement selectElem = waitForVisibility(roleSelect);
        highlightElement(selectElem);
        selectElem.sendKeys(role);
        click(btnSearch);
    }

    public int getUserRowsCount() {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }

    public String getFirstUserEmail() {
        return getText(firstRowEmail);
    }

    public String getFirstUserRole() {
        return getText(firstRowRole);
    }

    public boolean isSelfLockDisabled() {
        List<WebElement> buttons = driver.findElements(lockButtons);
        for (WebElement btn : buttons) {
            String title = btn.getAttribute("title");
            if (title != null && title.contains("Không thể khóa chính mình")) {
                highlightElement(btn);
                return !btn.isEnabled();
            }
        }
        return false;
    }

    public void clickOpenCreateModal() {
        click(btnOpenCreateModal);
        waitForVisibility(createFullNameInput);
    }

    public void enterCreateUserInfo(String name, String email, String phone, String password, String role) {
        type(createFullNameInput, name);
        type(createEmailInput, email);
        type(createPhoneInput, phone);
        type(createPasswordInput, password);
        if (role != null && !role.isEmpty()) {
            WebElement roleElem = waitForVisibility(createRoleSelect);
            highlightElement(roleElem);
            roleElem.sendKeys(role);
            delay();
        }
    }

    public void clickSubmitCreate() {
        click(btnSubmitCreate);
    }

    public void clickCancelModal() {
        click(btnCancelModal);
    }

    public void clickCancelCreateModal() {
        clickCancelModal();
    }

    public boolean isCreateModalOpen() {
        return isDisplayed(createFullNameInput);
    }

    public boolean isNoticeDisplayed() {
        return isDisplayed(noticeText);
    }
}

