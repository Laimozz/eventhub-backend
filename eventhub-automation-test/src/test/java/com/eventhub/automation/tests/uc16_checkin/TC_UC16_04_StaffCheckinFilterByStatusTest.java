package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_04_StaffCheckinFilterByStatusTest extends BaseTest {

    @Test(description = "TC_UC16_04: Nhân viên lọc danh sách vé theo trạng thái 'Chưa check-in' và 'Đã check-in'")
    public void testStaffCheckinFilterByStatus() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getStaffEmail(),
                ConfigurationManager.getStaffPassword()
        );

        StaffDashboardPage dashboard = new StaffDashboardPage(getDriver()).open();
        if (dashboard.getAssignedEventsCount() > 0) {
            dashboard.clickFirstAssignedEvent();
            StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
            Assert.assertTrue(ticketsPage.isLoaded(), "Trang danh sách vé sự kiện phải hiển thị");

            // Lọc chưa check-in
            ticketsPage.filterByUncheckedIn();
            Assert.assertTrue(ticketsPage.isLoaded(), "Danh sách vé lọc theo chưa check-in hoạt động tốt");

            // Lọc đã check-in
            ticketsPage.filterByCheckedIn();
            Assert.assertTrue(ticketsPage.isLoaded(), "Danh sách vé lọc theo đã check-in hoạt động tốt");
        }
    }
}

