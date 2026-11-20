package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_04_AdminCreateUserSuccessTest extends BaseTest {

    @Test(description = "TC_UC17_04: Quản trị viên thêm tài khoản người dùng mới thành công")
    public void testAdminCreateUserSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.clickOpenCreateModal();

        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@eventhub.test";
        usersPage.enterCreateUserInfo(
                "Người dùng Test Tự Động",
                uniqueEmail,
                "0988776655",
                "Password@123",
                "CUSTOMER"
        );

        usersPage.clickSubmitCreate();

        Assert.assertTrue(usersPage.isNoticeDisplayed() || !usersPage.isCreateModalOpen(),
                "Hệ thống phải tạo người dùng thành công và đóng form thêm người dùng");
    }
}

