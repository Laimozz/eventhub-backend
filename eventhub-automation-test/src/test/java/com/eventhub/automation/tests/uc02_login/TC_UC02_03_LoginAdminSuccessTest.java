package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_03_LoginAdminSuccessTest extends BaseTest {

    @Test(description = "TC_UC02_03: Đăng nhập thành công với vai trò Admin")
    public void testLoginAdminSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.get("admin.email", "seed.admin@eventhub.local"), ConfigurationManager.get("admin.password", "EventHub@123"));

        boolean redirected = ExpectedConditions.not(ExpectedConditions.urlContains("/login")).apply(getDriver());
        Assert.assertTrue(redirected, "Admin đăng nhập thành công phải được chuyển hướng");
    }
}

