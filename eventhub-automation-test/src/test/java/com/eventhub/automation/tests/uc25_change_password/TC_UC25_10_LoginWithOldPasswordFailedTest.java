package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_10_LoginWithOldPasswordFailedTest extends BaseTest {

    @Test(description = "TC25_10: Đăng nhập bằng mật khẩu cũ sau khi đổi - Từ chối đăng nhập")
    public void testLoginWithOldPasswordFailed() {
        String oldPass = ConfigurationManager.getCustomerPassword();
        String newPass = "NewPass@2026!";

        // 1. Đăng nhập và đổi sang mật khẩu mới
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), oldPass);

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword(oldPass, newPass, newPass);
        Assert.assertTrue(changePasswordPage.isToastOrSuccessDisplayed(),
                "Hệ thống phải hiển thị thông báo đổi mật khẩu thành công trước khi đăng xuất");

        // 2. Đăng xuất
        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        header.clickLogoutMenuItem();
        header.confirmLogout();

        // 3. Đăng nhập lại bằng mật khẩu cũ
        loginPage.open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), oldPass);

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                "Hệ thống phải từ chối đăng nhập bằng mật khẩu cũ và hiển thị thông báo lỗi");
    }
}

