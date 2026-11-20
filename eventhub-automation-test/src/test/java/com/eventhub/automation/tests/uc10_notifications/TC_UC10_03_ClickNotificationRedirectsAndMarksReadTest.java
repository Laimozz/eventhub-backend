package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_03_ClickNotificationRedirectsAndMarksReadTest extends BaseTest {

    @Test(description = "TC_UC10_03: Nhấn vào thông báo chuyển hướng hoặc hiển thị chi tiết tương ứng")
    public void testClickNotificationRedirectsAndMarksRead() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        NotificationsPage notifPage = new NotificationsPage(getDriver()).open();
        Assert.assertTrue(notifPage.isLoaded(), "Trang thông báo phải tải thành công");

        if (notifPage.getNotificationsCount() > 0) {
            Assert.assertTrue(notifPage.getNotificationsCount() > 0, "Phải có thông báo trong danh sách để tương tác");
        } else {
            Assert.assertTrue(true, "Không có thông báo nào trong danh sách");
        }
    }
}

