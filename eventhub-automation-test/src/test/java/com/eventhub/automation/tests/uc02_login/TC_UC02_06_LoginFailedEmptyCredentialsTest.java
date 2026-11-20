package com.eventhub.automation.tests.uc02_login;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC02_06_LoginFailedEmptyCredentialsTest extends BaseTest {

    @Test(description = "TC_UC02_06: Đăng nhập thất bại khi bỏ trống Email hoặc Mật khẩu")
    public void testLoginFailedEmptyCredentials() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        
        // Thử bỏ trống cả 2 và nhấn Submit
        loginPage.clickSubmit();

        // Kiểm tra trình duyệt không cho submit (HTML5 validation required hoặc alert)
        String currentUrl = loginPage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login"), "Vẫn phải ở lại trang /login khi để trống thông tin");
    }
}

