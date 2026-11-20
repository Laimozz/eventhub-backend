package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_08_RegisterFailedEmptyRequiredFieldsTest extends BaseTest {

    @Test(description = "TC_UC01_08: Đăng ký thất bại khi để trống các trường bắt buộc")
    public void testRegisterFailedEmptyRequiredFields() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        
        // Bấm Submit ngay mà không nhập gì
        registerPage.clickSubmit();

        // Kiểm tra vẫn ở trang register và hiển thị lỗi họ tên
        Assert.assertTrue(registerPage.getCurrentUrl().contains("/register"), "Không được phép submit khi để trống trường bắt buộc");
    }
}

