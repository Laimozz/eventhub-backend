package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_01_ChangePasswordSuccessTest extends BaseTest {

    @Test(description = "TC25_01: Đổi mật khẩu hợp lệ - Đổi mật khẩu thành công")
    public void testChangePasswordSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(), "NewPass@2026!", "NewPass@2026!");

        Assert.assertTrue(changePasswordPage.isToastOrSuccessDisplayed(),
                "Hệ thống phải hiển thị thông báo/toast đổi mật khẩu thành công");
    }
}

