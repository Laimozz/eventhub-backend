package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_02_StaffViewEventTicketsListTest extends BaseTest {

    @Test(description = "TC_UC16_02: Nhân viên xem chi tiết sự kiện và danh sách vé để sẵn sàng soát vé")
    public void testStaffViewEventTicketsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage staffDashboard = new StaffDashboardPage(getDriver()).open();
        staffDashboard.clickFirstAssignedEvent();

        StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
        Assert.assertTrue(ticketsPage.isLoaded(), "Trang danh sách vé sự kiện của Staff phải được mở");
    }
}

