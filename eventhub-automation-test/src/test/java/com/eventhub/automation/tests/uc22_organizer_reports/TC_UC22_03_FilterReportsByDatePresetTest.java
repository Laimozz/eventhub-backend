package com.eventhub.automation.tests.uc22_organizer_reports;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerReportsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC22_03_FilterReportsByDatePresetTest extends BaseTest {

    @Test(description = "TC_UC22_03: Chuyển đổi các mốc thời gian lọc báo cáo (7 ngày, 30 ngày, Tất cả)")
    public void testFilterReportsByDatePreset() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerReportsPage reportsPage = new OrganizerReportsPage(getDriver()).open();

        // Click lọc 7 ngày
        reportsPage.clickTimeRange("7 ngày");
        Assert.assertTrue(reportsPage.isRevenueMetricDisplayed(), "Báo cáo theo 7 ngày phải cập nhật hiển thị");

        // Click lọc Tất cả
        reportsPage.clickTimeRange("Tất cả");
        Assert.assertTrue(reportsPage.isRevenueMetricDisplayed(), "Báo cáo theo Tất cả thời gian phải cập nhật hiển thị");
    }
}

