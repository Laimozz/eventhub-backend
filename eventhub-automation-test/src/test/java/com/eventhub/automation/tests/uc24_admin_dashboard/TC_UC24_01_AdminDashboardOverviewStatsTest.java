package com.eventhub.automation.tests.uc24_admin_dashboard;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminDashboardPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC24_01_AdminDashboardOverviewStatsTest extends BaseTest {

    @Test(description = "TC_UC24_01: Quản trị viên mở trang Dashboard xem các chỉ số thống kê tài chính và sự kiện toàn sàn")
    public void testAdminDashboardOverviewStats() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminDashboardPage dashboardPage = new AdminDashboardPage(getDriver()).open();

        Assert.assertTrue(dashboardPage.isHeadingDisplayed(), "Tiêu đề Thống kê tổng quan Admin phải hiển thị");
        Assert.assertTrue(dashboardPage.isGrossRevenueMetricDisplayed(), "Thẻ doanh thu thanh toán thành công phải hiển thị");
        Assert.assertTrue(dashboardPage.isTotalEventsMetricDisplayed(), "Thẻ tổng số sự kiện phải hiển thị");
    }
}

