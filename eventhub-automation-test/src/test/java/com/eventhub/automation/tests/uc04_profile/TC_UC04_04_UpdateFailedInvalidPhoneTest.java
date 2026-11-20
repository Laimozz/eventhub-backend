package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_04_UpdateFailedInvalidPhoneTest extends BaseTest {

    @Test(description = "TC_UC04_04: Cập nhật thông tin thất bại khi Số điện thoại sai định dạng")
    public void testUpdateFailedInvalidPhone() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        // Nhập số điện thoại chứa ký tự chữ không hợp lệ
        profilePage.enterPhone("0378abc123");
        profilePage.clickSubmit();

        String error = profilePage.getFirstFieldError();
        Assert.assertTrue(error.contains("định dạng") || error.contains("chữ số"),
                "Hệ thống phải báo lỗi định dạng số điện thoại. Thực tế: " + error);
    }
}

