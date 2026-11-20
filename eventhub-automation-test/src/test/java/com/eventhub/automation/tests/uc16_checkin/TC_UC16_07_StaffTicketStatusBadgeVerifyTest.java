package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_07_StaffTicketStatusBadgeVerifyTest extends BaseTest {

    @Test(description = "TC_UC16_07: Kiểm tra danh sách vé và số liệu KPI check-in của sự kiện")
    public void testStaffTicketStatusBadgeVerify() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage dashboard = new StaffDashboardPage(getDriver()).open();
        if (dashboard.getAssignedEventsCount() > 0) {
            dashboard.clickFirstManageTickets();
            StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
            Assert.assertTrue(ticketsPage.isLoaded(), "Trang danh sách vé sự kiện phải hiển thị");
            Assert.assertTrue(ticketsPage.isKpiDisplayed(), "KPI thống kê check-in phải được hiển thị");
        }
    }
}

