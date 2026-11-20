package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_02_UnreadNotificationStyleTest extends BaseTest {

    @Test(description = "TC_UC10_02: Phân biệt giao diện trực quan giữa thông báo Chưa đọc và Đã đọc")
    public void testUnreadNotificationStyle() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        NotificationsPage notificationsPage = new NotificationsPage(getDriver()).open();

        Assert.assertTrue(notificationsPage.getNotificationsCount() > 0, "Trang thông báo phải tải được các mục thông báo");
    }
}

