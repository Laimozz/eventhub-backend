package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_06_NotificationBellBadgeCountTest extends BaseTest {

    @Test(description = "TC_UC10_06: Kiểm tra biểu tượng chuông thông báo trên thanh điều hướng Header")
    public void testNotificationBellBadgeCount() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        boolean bellDisplayed = getDriver().findElements(By.cssSelector("a[aria-label*='Thông báo'], a[href*='thong-bao']")).size() > 0;
        Assert.assertTrue(bellDisplayed, "Biểu tượng chuông thông báo phải được hiển thị trên header khi đã đăng nhập");
    }
}

