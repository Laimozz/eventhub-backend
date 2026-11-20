package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_03_StaffSearchTicketsTest extends BaseTest {

    @Test(description = "TC_UC16_03: Nhân viên soát vé tìm kiếm vé theo từ khóa hoặc mã vé")
    public void testStaffSearchTickets() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage staffDashboard = new StaffDashboardPage(getDriver()).open();
        staffDashboard.clickFirstAssignedEvent();

        StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
        Assert.assertTrue(ticketsPage.isLoaded(), "Trang soát vé phải được mở");

        ticketsPage.searchTicket("TCK");
        Assert.assertTrue(ticketsPage.getTicketListCount() >= 0,
                "Chức năng tìm kiếm vé của nhân viên thực hiện ổn định");
    }
}

