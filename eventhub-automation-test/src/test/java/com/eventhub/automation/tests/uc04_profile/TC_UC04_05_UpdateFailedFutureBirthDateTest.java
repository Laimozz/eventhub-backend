package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_05_UpdateFailedFutureBirthDateTest extends BaseTest {

    @Test(description = "TC_UC04_05: Cập nhật thông tin thất bại khi Ngày sinh lớn hơn ngày hiện tại")
    public void testUpdateFailedFutureBirthDate() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        // Nhập ngày sinh trong tương lai (năm 2030)
        profilePage.enterBirthDate("2030-01-01");
        profilePage.clickSubmit();

        String error = profilePage.getFirstFieldError();
        Assert.assertTrue(error.contains("trước ngày hiện tại") || error.contains("Ngày sinh"),
                "Hệ thống phải báo lỗi khi ngày sinh lớn hơn ngày hiện tại. Thực tế: " + error);
    }
}

