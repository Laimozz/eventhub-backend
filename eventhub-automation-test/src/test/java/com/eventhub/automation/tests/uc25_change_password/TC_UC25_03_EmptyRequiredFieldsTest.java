package com.eventhub.automation.tests.uc25_change_password;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.ChangePasswordPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC25_03_EmptyRequiredFieldsTest extends BaseTest {

    @Test(description = "TC25_03: Để trống các trường bắt buộc - Yêu cầu nhập đầy đủ thông tin")
    public void testEmptyRequiredFields() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ChangePasswordPage changePasswordPage = new ChangePasswordPage(getDriver()).open();
        changePasswordPage.clickSubmit();

        Assert.assertTrue(changePasswordPage.isFieldErrorDisplayed(),
                "Hệ thống phải hiển thị thông báo yêu cầu nhập đầy đủ thông tin các trường bắt buộc");
    }
}

