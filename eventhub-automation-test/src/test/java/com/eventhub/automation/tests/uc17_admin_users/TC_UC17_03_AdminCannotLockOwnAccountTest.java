package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_03_AdminCannotLockOwnAccountTest extends BaseTest {

    @Test(description = "TC_UC17_03: Quản trị viên không thể tự khóa tài khoản chính mình (nút khóa bị disabled)")
    public void testAdminCannotLockOwnAccount() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();

        // Tìm kiếm chính tài khoản admin đang đăng nhập
        usersPage.searchUser(ConfigurationManager.getAdminEmail());

        Assert.assertTrue(usersPage.isSelfLockDisabled(),
                "Nút khóa tài khoản của chính admin phải bị vô hiệu hóa kèm thông báo 'Không thể khóa chính mình'");
    }
}

