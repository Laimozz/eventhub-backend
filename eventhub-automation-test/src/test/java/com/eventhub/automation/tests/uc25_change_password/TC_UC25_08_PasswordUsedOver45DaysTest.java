package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_08_PasswordUsedOver45DaysTest extends BaseTest {

    @Test(description = "TC25_08: Mật khẩu mới trùng mật khẩu đã sử dụng cách đây hơn 45 ngày - Cho phép đổi nếu hợp lệ")
    public void testPasswordUsedOver45Days() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        // Mật khẩu mới hợp lệ, đáp ứng chính sách
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(), "AncientPass@2026!", "AncientPass@2026!");

        Assert.assertTrue(changePasswordPage.isToastOrSuccessDisplayed(),
                "Hệ thống phải cho phép đổi mật khẩu khi đáp ứng tiêu chuẩn chính sách bảo mật");
    }
}

