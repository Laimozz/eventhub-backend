package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_07_EmptyPendingEventsStateTest extends BaseTest {

    @Test(description = "TC_UC19_07: Kiểm tra giao diện trạng thái trống khi không có sự kiện nào trong danh sách")
    public void testEmptyPendingEventsState() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).openWithQuery("?page=999");
        Assert.assertTrue(pendingPage.isLoaded(), "Trang danh sách sự kiện chờ duyệt phải tải thành công");
        Assert.assertTrue(pendingPage.hasEmptyMessage() || pendingPage.getPendingEventsCount() == 0,
                "Hệ thống phải hiển thị trạng thái trống hoặc không có dòng dữ liệu nào");
    }
}
