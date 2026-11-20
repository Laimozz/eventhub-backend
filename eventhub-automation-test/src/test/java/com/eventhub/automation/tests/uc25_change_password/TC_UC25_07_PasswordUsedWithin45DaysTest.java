package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_07_PasswordUsedWithin45DaysTest extends BaseTest {

    @Test(description = "TC25_07: Mật khẩu mới trùng mật khẩu đã sử dụng trong 45 ngày gần nhất - Từ chối đổi mật khẩu")
    public void testPasswordUsedWithin45Days() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        // Thử đổi về mật khẩu vừa sử dụng gần đây
        changePasswordPage.changePassword(ConfigurationManager.getCustomerPassword(),
                ConfigurationManager.getCustomerPassword(), ConfigurationManager.getCustomerPassword());

        boolean hasError = changePasswordPage.waitForToastOrError();
        Assert.assertTrue(hasError,
                "Hệ thống phải từ chối khi mật khẩu mới trùng với mật khẩu đã sử dụng gần nhất");
    }
}

