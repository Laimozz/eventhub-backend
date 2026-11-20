package com.eventhub.automation.tests.uc10_notifications;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC10_05_MarkAllNotificationsReadTest extends BaseTest {

    @Test(description = "TC_UC10_05: Đánh dấu tất cả thông báo là đã đọc thành công")
    public void testMarkAllNotificationsRead() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        NotificationsPage notificationsPage = new NotificationsPage(getDriver()).open();

        if (notificationsPage.isMarkAllReadButtonEnabled()) {
            notificationsPage.clickMarkAllRead();
            // Sau khi bấm mark all read, nút sẽ chuyển sang disabled
            Assert.assertFalse(notificationsPage.isMarkAllReadButtonEnabled(),
                    "Nút 'Đánh dấu tất cả đã đọc' phải bị vô hiệu hóa khi không còn thông báo chưa đọc");
        } else {
            // Trường hợp tài khoản ban đầu đã không còn thông báo chưa đọc
            Assert.assertFalse(notificationsPage.isMarkAllReadButtonEnabled());
        }
    }
}

