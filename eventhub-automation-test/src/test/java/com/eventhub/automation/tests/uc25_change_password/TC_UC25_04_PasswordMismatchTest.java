package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_04_PasswordMismatchTest extends BaseTest {

    @Test(description = "TC25_04: Mật khẩu mới và xác nhận không khớp - Hiển thị thông báo không khớp")
    public void testPasswordMismatch() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(), "NewPass@2026!", "DifferentPass@2026!");

        String error = changePasswordPage.getFieldError();
        Assert.assertTrue(error.contains("không trùng khớp") || error.contains("không khớp") || changePasswordPage.isFieldErrorDisplayed(),
                "Hệ thống phải hiển thị thông báo mật khẩu xác nhận không khớp");
    }
}

