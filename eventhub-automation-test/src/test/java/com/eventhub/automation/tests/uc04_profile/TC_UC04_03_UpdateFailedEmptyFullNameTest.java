package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_03_UpdateFailedEmptyFullNameTest extends BaseTest {

    @Test(description = "TC_UC04_03: Cập nhật thông tin thất bại khi để trống Họ và tên")
    public void testUpdateFailedEmptyFullName() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        // Xóa trống họ tên và submit
        profilePage.enterFullName("");
        profilePage.clickSubmit();

        String error = profilePage.getFirstFieldError();
        Assert.assertTrue(error.contains("không được để trống"), "Phải báo lỗi khi Họ và tên để trống. Thực tế: " + error);
    }
}

