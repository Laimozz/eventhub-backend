package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_06_AdminSearchUserNotFoundTest extends BaseTest {

    @Test(description = "TC_UC17_06: Quản trị viên tìm kiếm người dùng với email không tồn tại trong hệ thống")
    public void testAdminSearchUserNotFound() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.searchUser("email_khong_ton_tai_9999@domain.test");

        Assert.assertEquals(usersPage.getUserRowsCount(), 0, "Không được hiển thị bản ghi nào khi tìm kiếm email không tồn tại");
    }
}

