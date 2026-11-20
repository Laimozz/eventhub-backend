package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_09_AdminCancelCreateUserModalTest extends BaseTest {

    @Test(description = "TC_UC17_09: Đóng modal Thêm người dùng không lưu dữ liệu")
    public void testAdminCancelCreateUserModal() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.clickOpenCreateModal();
        Assert.assertTrue(usersPage.isCreateModalOpen(), "Form thêm người dùng phải mở");

        usersPage.clickCancelCreateModal();
        Assert.assertFalse(usersPage.isCreateModalOpen(), "Form thêm người dùng phải được đóng lại");
    }
}

