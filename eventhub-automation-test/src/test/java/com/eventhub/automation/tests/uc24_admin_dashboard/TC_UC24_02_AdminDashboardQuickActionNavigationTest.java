package com.eventhub.automation.tests.uc24_admin_dashboard;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminDashboardPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC24_02_AdminDashboardQuickActionNavigationTest extends BaseTest {

    @Test(description = "TC_UC24_02: Quản trị viên điều hướng nhanh từ Dashboard sang trang Quản trị Người dùng")
    public void testAdminDashboardQuickActionNavigation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminDashboardPage dashboardPage = new AdminDashboardPage(getDriver()).open();
        dashboardPage.navigateToUsers();

        String currentUrl = dashboardPage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/admin/users"), "Phải điều hướng thành công đến trang /admin/users");
    }
}

