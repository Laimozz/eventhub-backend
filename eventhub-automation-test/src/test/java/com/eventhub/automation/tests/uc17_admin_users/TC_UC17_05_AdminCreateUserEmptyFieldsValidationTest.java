package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_05_AdminCreateUserEmptyFieldsValidationTest extends BaseTest {

    @Test(description = "TC_UC17_05: Quản trị viên thêm người dùng thất bại khi để trống thông tin bắt buộc")
    public void testAdminCreateUserEmptyFieldsValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();
        usersPage.clickOpenCreateModal();

        // Để trống các trường và bấm Tạo người dùng
        usersPage.clickSubmitCreate();

        // Modal vẫn phải mở do thuộc tính required chặn form
        Assert.assertTrue(usersPage.isCreateModalOpen(),
                "Hộp thoại thêm người dùng không được gửi khi các trường bắt buộc để trống");

        usersPage.clickCancelModal();
    }
}

