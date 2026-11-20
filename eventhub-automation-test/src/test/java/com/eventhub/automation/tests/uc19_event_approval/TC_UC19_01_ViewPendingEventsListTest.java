package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_01_ViewPendingEventsListTest extends BaseTest {

    @Test(description = "TC_UC19_01: Quản trị viên xem danh sách các hồ sơ sự kiện đang chờ phê duyệt")
    public void testViewPendingEventsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();

        Assert.assertTrue(pendingPage.getPendingEventsCount() > 0, "Hệ thống phải có danh sách sự kiện chờ duyệt hiển thị");
    }
}

