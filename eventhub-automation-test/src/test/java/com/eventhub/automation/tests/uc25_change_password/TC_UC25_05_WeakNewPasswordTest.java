package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_05_WeakNewPasswordTest extends BaseTest {

    @Test(description = "TC25_05: Mật khẩu mới không đáp ứng yêu cầu - Từ chối đổi mật khẩu")
    public void testWeakNewPassword() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        // Mật khẩu yếu chỉ có chữ thường
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(), "weakpass", "weakpass");

        Assert.assertTrue(changePasswordPage.isFieldErrorDisplayed(),
                "Hệ thống phải hiển thị thông báo lỗi yêu cầu độ phức tạp của mật khẩu mới");
    }
}

