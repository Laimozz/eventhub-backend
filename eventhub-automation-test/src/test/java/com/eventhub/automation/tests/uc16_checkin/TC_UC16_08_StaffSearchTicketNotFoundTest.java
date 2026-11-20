package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_08_StaffSearchTicketNotFoundTest extends BaseTest {

    @Test(description = "TC_UC16_08: Tìm kiếm vé với mã vé không tồn tại")
    public void testStaffSearchTicketNotFound() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getStaffEmail(), ConfigurationManager.getStaffPassword());

        StaffDashboardPage dashboard = new StaffDashboardPage(getDriver()).open();
        if (dashboard.getAssignedEventsCount() > 0) {
            dashboard.clickFirstManageTickets();
            StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
            Assert.assertTrue(ticketsPage.isLoaded(), "Trang danh sách vé sự kiện phải hiển thị");
            ticketsPage.searchTicket("NON_EXISTENT_TICKET_CODE_99999");
            Assert.assertTrue(ticketsPage.isLoaded(), "Giao diện phải giữ nguyên trạng thái sau khi tìm kiếm");
        }
    }
}

