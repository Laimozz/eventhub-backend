package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_02_IncorrectCurrentPasswordTest extends BaseTest {

    @Test(description = "TC25_02: Nhập sai mật khẩu hiện tại - Hiển thị thông báo lỗi")
    public void testIncorrectCurrentPassword() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword("WrongPassword@999", "NewPass@2026!", "NewPass@2026!");

        boolean hasError = changePasswordPage.waitForToastOrError();
        Assert.assertTrue(hasError, "Hệ thống phải hiển thị thông báo lỗi khi nhập sai mật khẩu hiện tại");
    }
}

