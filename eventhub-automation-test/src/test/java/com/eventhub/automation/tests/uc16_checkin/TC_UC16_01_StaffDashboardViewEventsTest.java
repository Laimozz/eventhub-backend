package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_01_StaffDashboardViewEventsTest extends BaseTest {

    @Test(description = "TC_UC16_01: Nhân viên soát vé đăng nhập và xem danh sách sự kiện được phân công")
    public void testStaffDashboardViewEvents() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage staffDashboard = new StaffDashboardPage(getDriver()).open();

        Assert.assertTrue(staffDashboard.getCurrentUrl().contains("/staff"), "Nhân viên phải được chuyển hướng đến /staff");
        Assert.assertTrue(staffDashboard.getAssignedEventsCount() > 0, "Nhân viên phải có sự kiện được phân công trong danh sách");
    }
}

