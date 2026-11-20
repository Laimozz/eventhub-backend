package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import com.eventhub.automation.utils.DataHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_06_RegisterFailedPasswordMismatchTest extends BaseTest {

    @Test(description = "TC_UC01_06: Đăng ký thất bại khi Xác nhận mật khẩu không khớp")
    public void testRegisterPasswordMismatch() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        registerPage.register("CUSTOMER", "Nguyễn Văn Test", DataHelper.generateRandomEmail(), null, "Password123@", "DifferentPassword456@");

        Assert.assertEquals(registerPage.getConfirmPasswordError(), "Mật khẩu xác nhận chưa khớp.");
    }
}

