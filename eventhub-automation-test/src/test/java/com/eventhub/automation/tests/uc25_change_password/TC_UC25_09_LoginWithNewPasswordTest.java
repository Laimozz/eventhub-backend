package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_09_LoginWithNewPasswordTest extends BaseTest {

    @Test(description = "TC25_09: Đăng nhập bằng mật khẩu mới sau khi đổi - Đăng nhập thành công")
    public void testLoginWithNewPassword() {
        String newPass = "NewPass@2026!";

        // 1. Đăng nhập và đổi mật khẩu
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(), newPass, newPass);
        Assert.assertTrue(changePasswordPage.isToastOrSuccessDisplayed(),
                "Hệ thống phải hiển thị thông báo đổi mật khẩu thành công trước khi đăng xuất");

        // 2. Đăng xuất
        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        header.clickLogoutMenuItem();
        header.confirmLogout();

        // 3. Đăng nhập lại với mật khẩu mới
        loginPage.open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), newPass);

        Assert.assertTrue(header.isUserLoggedIn(), "Người dùng phải đăng nhập thành công bằng mật khẩu mới");
    }
}

