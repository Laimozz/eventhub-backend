package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_08_LoginNavigateToRegisterTest extends BaseTest {

    @Test(description = "TC_UC02_08: Kiểm tra liên kết chuyển hướng sang trang Đăng ký")
    public void testLoginNavigateToRegister() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.clickRegisterLink();

        boolean onRegisterPage = ExpectedConditions.urlContains("/register").apply(getDriver());
        Assert.assertTrue(onRegisterPage, "Nhấn 'Đăng ký ngay' phải chuyển sang đường dẫn /register");
    }
}

