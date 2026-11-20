package com.eventhub.automation.tests.uc01_register;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.RegisterPage;
import com.eventhub.automation.utils.DataHelper;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC01_02_RegisterOrganizerSuccessTest extends BaseTest {

    @Test(description = "TC_UC01_02: Đăng ký thành công tài khoản Ban tổ chức (Organizer)")
    public void testRegisterOrganizerSuccess() {
        RegisterPage registerPage = new RegisterPage(getDriver()).open();
        String randomEmail = DataHelper.generateRandomEmail();
        String randomName = "Công Ty Tổ Chức " + System.currentTimeMillis();

        registerPage.register("ORGANIZER", randomName, randomEmail, null, "OrganizerPass123@", "OrganizerPass123@");

        boolean onLoginPage = new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(onLoginPage, "Đăng ký Organizer thành công phải chuyển về trang /login");
    }
}

