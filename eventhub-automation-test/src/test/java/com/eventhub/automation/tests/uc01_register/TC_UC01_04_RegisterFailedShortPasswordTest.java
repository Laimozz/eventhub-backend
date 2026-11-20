package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import com.eventhub.automation.utils.DataHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_04_RegisterFailedShortPasswordTest extends BaseTest {

    @Test(description = "TC_UC01_04: Đăng ký thất bại khi mật khẩu ngắn hơn 8 ký tự")
    public void testRegisterFailedShortPassword() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        // Mật khẩu 6 ký tự
        registerPage.register("CUSTOMER", "Nguyễn Văn Test", DataHelper.generateRandomEmail(), null, "Pass1@", "Pass1@");

        Assert.assertTrue(registerPage.getPasswordError().contains("ít nhất 8 ký tự"), "Phải hiển thị cảnh báo mật khẩu cần ít nhất 8 ký tự");
    }
}

