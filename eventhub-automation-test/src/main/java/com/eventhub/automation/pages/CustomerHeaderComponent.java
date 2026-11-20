package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CustomerHeaderComponent extends BasePage {
    private final By accountMenuButton = By.cssSelector("button[aria-controls='customer-account-menu'], button[class*='userMenuBtn']");
    private final By accountDropdown = By.id("customer-account-menu");
    private final By profileMenuItem = By.xpath("//button[contains(., 'Thông tin cá nhân')]");
    private final By changePasswordMenuItem = By.xpath("//button[contains(., 'Đổi mật khẩu')]");
    private final By myBookingsMenuItem = By.cssSelector("a[href='/customer/booking-cua-toi']");
    private final By myTicketsMenuItem = By.cssSelector("a[href='/customer/ve-cua-toi']");
    private final By notificationsMenuItem = By.cssSelector("a[href='/customer/thong-bao']");
    private final By logoutMenuItem = By.cssSelector("button[class*='danger']");

    // Modal xác nhận đăng xuất
    private final By logoutModal = By.xpath("//h3[contains(text(), 'Xác nhận đăng xuất')]");
    private final By confirmLogoutButton = By.cssSelector("button[class*='btnConfirmLogout']");
    private final By cancelLogoutButton = By.cssSelector("button[class*='btnCancel']");

    // Guest actions
    private final By loginButton = By.cssSelector("a[href='/login']");
    private final By registerButton = By.cssSelector("a[href='/register']");
    private final By exploreNavLink = By.cssSelector("a[href='/customer/explore']");

    public CustomerHeaderComponent(WebDriver driver) {
        super(driver);
    }

    public boolean isUserLoggedIn() {
        return isDisplayed(accountMenuButton);
    }

    public void openAccountMenu() {
        click(accountMenuButton);
        waitForVisibility(accountDropdown);
    }

    public boolean isAccountDropdownDisplayed() {
        return isDisplayed(accountDropdown);
    }

    public boolean isProfileItemDisplayed() {
        return isDisplayed(profileMenuItem);
    }

    public boolean isMyBookingsItemDisplayed() {
        return isDisplayed(myBookingsMenuItem);
    }

    public boolean isMyTicketsItemDisplayed() {
        return isDisplayed(myTicketsMenuItem);
    }

    public void clickLogoutMenuItem() {
        openAccountMenu();
        click(logoutMenuItem);
        waitForVisibility(logoutModal);
    }

    public boolean isLogoutModalDisplayed() {
        return isDisplayed(logoutModal);
    }

    public void confirmLogout() {
        click(confirmLogoutButton);
    }

    public void cancelLogout() {
        click(cancelLogoutButton);
    }

    public boolean isLoginButtonDisplayed() {
        return isDisplayed(loginButton);
    }

    public void clickExplore() {
        click(exploreNavLink);
    }
}

