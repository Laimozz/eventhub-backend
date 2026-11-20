package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_06_StaffCheckinBackNavigationTest extends BaseTest {

    @Test(description = "TC_UC16_06: Nhân viên xem danh sách sự kiện được phân công soát vé")
    public void testStaffCheckinBackNavigation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage dashboard = new StaffDashboardPage(getDriver()).open();
        Assert.assertTrue(dashboard.isLoaded(), "Trang điều phối check-in của nhân viên phải hiển thị");
        Assert.assertTrue(dashboard.getAssignedEventsCount() > 0, "Nhân viên phải có sự kiện được phân công");
    }
}

