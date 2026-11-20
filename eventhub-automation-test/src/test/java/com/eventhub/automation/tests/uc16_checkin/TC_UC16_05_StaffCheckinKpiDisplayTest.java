package com.eventhub.automation.tests.uc16_checkin;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDashboardPage;
import com.eventhub.automation.pages.StaffEventTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC16_05_StaffCheckinKpiDisplayTest extends BaseTest {

    @Test(description = "TC_UC16_05: Kiểm tra bảng thống kê tỷ lệ check-in và số vé đã quét thành công")
    public void testStaffCheckinKpiDisplay() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getStaffEmail(),
                ConfigurationManager.getStaffPassword()
        );

        StaffDashboardPage dashboard = new StaffDashboardPage(getDriver()).open();
        if (dashboard.getAssignedEventsCount() > 0) {
            dashboard.clickFirstAssignedEvent();
            StaffEventTicketsPage ticketsPage = new StaffEventTicketsPage(getDriver());
            Assert.assertTrue(ticketsPage.isKpiDisplayed(), "Thẻ KPI thống kê số vé check-in và chưa check-in phải hiển thị");
        }
    }
}

