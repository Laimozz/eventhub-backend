package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_02_AdminFilterUserByRoleTest extends BaseTest {

    @Test(description = "TC_UC17_02: Quản trị viên lọc danh sách người dùng theo vai trò ORGANIZER")
    public void testAdminFilterUserByRole() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();

        usersPage.filterRole("ORGANIZER");

        Assert.assertTrue(usersPage.getUserRowsCount() > 0, "Bảng danh sách phải trả về người dùng có vai trò ORGANIZER");
        Assert.assertEquals(usersPage.getFirstUserRole(), "ORGANIZER", "Vai trò của tài khoản trong bảng phải là ORGANIZER");
    }
}

