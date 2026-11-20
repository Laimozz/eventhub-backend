package com.eventhub.automation.tests.uc22_organizer_reports;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerReportsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC22_01_ViewOrganizerReportsOverviewTest extends BaseTest {

    @Test(description = "TC_UC22_01: Ban tổ chức mở trang Báo cáo & Thống kê xem tổng quan số liệu doanh thu và vé bán")
    public void testViewOrganizerReportsOverview() {
        // Đăng nhập tài khoản Organizer
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        // Mở trang báo cáo
        OrganizerReportsPage reportsPage = new OrganizerReportsPage(getDriver()).open();

        Assert.assertTrue(reportsPage.isPageTitleDisplayed(), "Tiêu đề trang Báo cáo & Thống kê phải được hiển thị");
        Assert.assertTrue(reportsPage.isRevenueMetricDisplayed(), "Chỉ số doanh thu phải hiển thị");
        Assert.assertTrue(reportsPage.isTicketsSoldMetricDisplayed(), "Chỉ số vé bán ra phải hiển thị");
    }
}

