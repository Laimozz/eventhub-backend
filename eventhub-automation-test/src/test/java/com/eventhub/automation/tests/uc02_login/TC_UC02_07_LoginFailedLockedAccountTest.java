package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_07_LoginFailedLockedAccountTest extends BaseTest {

    @Test(description = "TC_UC02_07: Đăng nhập thất bại với tài khoản đang ở trạng thái Bị khóa (LOCKED)")
    public void testLoginFailedLockedAccount() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        // Tài khoản vol.customer36@eventhub.test đã được seed trạng thái LOCKED
        loginPage.login(ConfigurationManager.getLockedCustomerEmail(), "EventHub@123");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Phải hiển thị thông báo lỗi khi tài khoản bị khóa");
    }
}

