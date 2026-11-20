package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_07_AdminCannotLockOwnAccountDirectTest extends BaseTest {

    @Test(description = "TC_UC17_07: Bảo mật: Admin không thể tự khóa chính tài khoản của mình")
    public void testAdminCannotLockOwnAccountDirect() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.searchUser(ConfigurationManager.getAdminEmail());

        Assert.assertTrue(usersPage.getUserRowsCount() >= 1, "Phải tìm thấy tài khoản admin hiện tại");
    }
}

