package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_03_RegisterFailedInvalidEmailTest extends BaseTest {

    @Test(description = "TC_UC01_03: Đăng ký thất bại khi nhập sai định dạng Email")
    public void testRegisterFailedInvalidEmail() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        // Nhập email sai cú pháp: không có @
        registerPage.register("CUSTOMER", "Nguyễn Văn Test", "email_sai_dinh_dang", "0909123456", "SecurePass123@", "SecurePass123@");

        // Kiểm tra vẫn ở lại trang đăng ký
        Assert.assertTrue(registerPage.getCurrentUrl().contains("/register"), "Vẫn phải ở lại trang /register khi email sai định dạng");
    }
}

