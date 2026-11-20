package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.RegisterPage;
import com.eventhub.automation.utils.DataHelper;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_01_RegisterCustomerSuccessTest extends BaseTest {

    @Test(description = "TC_UC01_01: Đăng ký thành công tài khoản Khách mua vé (Customer) với dữ liệu hợp lệ")
    public void testRegisterCustomerSuccess() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        String randomEmail = DataHelper.generateRandomEmail();
        String randomName = DataHelper.generateRandomName();
        String randomPhone = DataHelper.generateRandomPhone();

        registerPage.register("CUSTOMER", randomName, randomEmail, randomPhone, "SecurePass123@", "SecurePass123@");

        LoginPage loginPage = new LoginPage(getDriver());
        boolean onLoginPage = new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(onLoginPage, "Đăng ký thành công phải chuyển về trang Đăng nhập");
        Assert.assertTrue(loginPage.isSuccessNoticeDisplayed(), "Phải hiển thị thông báo Đăng ký thành công");
    }
}

