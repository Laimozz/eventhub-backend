package com.eventhub.automation.tests.uc17_admin_users;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminUsersPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC17_01_AdminSearchUserByEmailTest extends BaseTest {

    @Test(description = "TC_UC17_01: Quản trị viên tìm kiếm người dùng theo email hiển thị kết quả chính xác")
    public void testAdminSearchUserByEmail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminUsersPage usersPage = new AdminUsersPage(getDriver()).open();

        String targetEmail = ConfigurationManager.getCustomerEmail();
        usersPage.searchUser(targetEmail);

        Assert.assertTrue(usersPage.getUserRowsCount() > 0, "Bảng danh sách phải trả về ít nhất 1 dòng kết quả tìm kiếm");
        Assert.assertEquals(usersPage.getFirstUserEmail(), targetEmail, "Dòng kết quả đầu tiên phải trùng khớp email tìm kiếm");
    }
}

