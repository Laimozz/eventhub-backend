package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_01_ViewNotificationsListTest extends BaseTest {

    @Test(description = "TC_UC10_01: Hiển thị danh sách thông báo của tài khoản người dùng")
    public void testViewNotificationsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        NotificationsPage notificationsPage = new NotificationsPage(getDriver()).open();

        Assert.assertTrue(notificationsPage.getNotificationsCount() > 0, "Danh sách thông báo phải tải được các thông báo của người dùng");
    }
}

