package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_01_LoginCustomerSuccessTest extends BaseTest {

    @Test(description = "TC_UC02_01: Đăng nhập thành công với vai trò Customer bằng thông tin chính xác")
    public void testLoginCustomerSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        boolean redirected = ExpectedConditions.not(ExpectedConditions.urlContains("/login")).apply(getDriver());
        Assert.assertTrue(redirected, "Sau khi đăng nhập thành công phải được chuyển hướng khỏi /login");
    }
}

