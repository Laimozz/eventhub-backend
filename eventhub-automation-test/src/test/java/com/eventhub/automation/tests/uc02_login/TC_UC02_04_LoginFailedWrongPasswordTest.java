package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_04_LoginFailedWrongPasswordTest extends BaseTest {

    @Test(description = "TC_UC02_04: Đăng nhập thất bại khi nhập sai Mật khẩu")
    public void testLoginFailedWrongPassword() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), "SaiMatKhau123@");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Phải hiển thị thông báo lỗi khi nhập sai mật khẩu");
        Assert.assertFalse(loginPage.getErrorMessage().isEmpty(), "Nội dung thông báo lỗi không được rỗng");
    }
}

