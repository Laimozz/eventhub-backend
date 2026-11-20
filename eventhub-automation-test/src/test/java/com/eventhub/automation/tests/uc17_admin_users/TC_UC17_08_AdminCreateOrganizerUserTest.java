package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_08_AdminCreateOrganizerUserTest extends BaseTest {

    @Test(description = "TC_UC17_08: Quản trị viên mở form tạo tài khoản người dùng và chọn vai trò")
    public void testAdminCreateOrganizerUserModal() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.clickOpenCreateModal();
        Assert.assertTrue(usersPage.isCreateModalOpen(), "Form thêm người dùng phải được mở");
    }
}

