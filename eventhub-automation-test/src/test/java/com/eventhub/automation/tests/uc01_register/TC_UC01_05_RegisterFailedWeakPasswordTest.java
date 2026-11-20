package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import com.eventhub.automation.utils.DataHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_05_RegisterFailedWeakPasswordTest extends BaseTest {

    @Test(description = "TC_UC01_05: Đăng ký thất bại khi mật khẩu không đủ độ dài hoặc chỉ toàn khoảng trắng")
    public void testRegisterFailedWeakPassword() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        // Mật khẩu toàn khoảng trắng
        registerPage.register("CUSTOMER", "Nguyễn Văn Test", DataHelper.generateRandomEmail(), null, "        ", "        ");

        Assert.assertTrue(registerPage.getPasswordError().contains("ít nhất 8 ký tự") || registerPage.getPasswordError().contains("khoảng trắng"),
                "Phải hiển thị cảnh báo khi mật khẩu không hợp lệ");
    }
}

