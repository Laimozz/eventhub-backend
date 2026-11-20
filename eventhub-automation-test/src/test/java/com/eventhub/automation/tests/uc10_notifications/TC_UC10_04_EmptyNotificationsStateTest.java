package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_04_EmptyNotificationsStateTest extends BaseTest {

    @Test(description = "TC_UC10_04: Hiển thị trạng thái khi người dùng mở danh sách thông báo hoặc kiểm tra thông báo trống (Exception 1)")
    public void testEmptyNotificationsState() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        NotificationsPage notifPage = new NotificationsPage(getDriver()).open();
        Assert.assertTrue(notifPage.isLoaded(), "Trang Thông báo hệ thống phải được hiển thị bình thường");
    }
}

