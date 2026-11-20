package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_01_ViewProfileDetailsTest extends BaseTest {

    @Test(description = "TC_UC04_01: Xem thông tin cá nhân hiển thị chính xác từ cơ sở dữ liệu")
    public void testViewProfileDetails() {
        // Đăng nhập tài khoản Customer
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        // Điều hướng tới trang Thông tin cá nhân
        ProfilePage profilePage = new ProfilePage(getDriver()).open();

        String headline = profilePage.getHeadlineTitle();
        Assert.assertTrue(headline.contains("Thông tin cá nhân"), "Tiêu đề trang phải là 'Thông tin cá nhân'");

        String fullName = profilePage.getHeroFullName();
        Assert.assertFalse(fullName.isEmpty(), "Họ tên người dùng không được rỗng");
    }
}

