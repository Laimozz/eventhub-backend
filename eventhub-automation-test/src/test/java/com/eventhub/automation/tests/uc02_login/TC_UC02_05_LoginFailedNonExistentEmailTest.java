package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_05_LoginFailedNonExistentEmailTest extends BaseTest {

    @Test(description = "TC_UC02_05: Đăng nhập thất bại khi Email chưa tồn tại trong hệ thống")
    public void testLoginFailedNonExistentEmail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login("notfound_user_99999@eventhub.test", "Pass123456@");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Phải hiển thị lỗi khi email chưa đăng ký");
    }
}

