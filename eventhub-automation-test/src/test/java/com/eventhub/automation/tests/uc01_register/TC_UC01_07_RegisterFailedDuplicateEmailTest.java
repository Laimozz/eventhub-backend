package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_07_RegisterFailedDuplicateEmailTest extends BaseTest {

    @Test(description = "TC_UC01_07: Đăng ký thất bại khi Email đã tồn tại trên hệ thống (Duplicate Email)")
    public void testRegisterDuplicateEmail() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        // Dùng email đã tồn tại sẵn trong CSDL
        String existingEmail = ConfigurationManager.getCustomerEmail();

        registerPage.register("CUSTOMER", "Người Dùng Trùng", existingEmail, null, "Password123@", "Password123@");

        Assert.assertTrue(registerPage.isAlertErrorDisplayed(), "Phải hiển thị thông báo lỗi trùng email");
    }
}

