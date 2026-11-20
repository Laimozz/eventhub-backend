package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_02_LoginOrganizerSuccessTest extends BaseTest {

    @Test(description = "TC_UC02_02: Đăng nhập thành công với vai trò Organizer và điều hướng đúng")
    public void testLoginOrganizerSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        boolean redirected = ExpectedConditions.not(ExpectedConditions.urlContains("/login")).apply(getDriver());
        Assert.assertTrue(redirected, "Organizer đăng nhập thành công phải được chuyển hướng");
    }
}

