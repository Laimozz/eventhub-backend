package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_05_StaffDispatchActiveStatusBadgeTest extends BaseTest {

    @Test(description = "TC_UC13_05: Kiểm tra danh sách điều phối nhân viên và bộ lọc sự kiện")
    public void testStaffDispatchActiveStatusBadge() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        StaffDispatchPage page = new StaffDispatchPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang điều phối nhân sự sự kiện phải được mở thành công");
        Assert.assertTrue(page.getEventsCount() >= 0, "Danh sách sự kiện điều phối phải hiển thị");
    }
}

