package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_06_NewPasswordSameAsCurrentTest extends BaseTest {

    @Test(description = "TC25_06: Mật khẩu mới trùng mật khẩu hiện tại - Từ chối đổi mật khẩu")
    public void testNewPasswordSameAsCurrent() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(),
                ConfigurationManager.getCustomerPassword(), ConfigurationManager.getCustomerPassword());

        boolean hasError = changePasswordPage.waitForToastOrError();
        Assert.assertTrue(hasError, "Hệ thống phải từ chối đổi mật khẩu khi mật khẩu mới trùng mật khẩu hiện tại");
    }
}

