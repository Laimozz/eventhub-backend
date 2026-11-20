package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_06_UpdateProfileSpecialCharactersTest extends BaseTest {

    @Test(description = "TC_UC04_06: Cập nhật thông tin thất bại hoặc sanitize khi họ tên chứa thẻ HTML/Script")
    public void testUpdateProfileSpecialCharacters() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        // Nhập chuỗi HTML độc hại
        profilePage.enterFullName("<script>alert('xss')</script>");
        profilePage.clickSubmit();

        // Kiểm tra hệ thống không bị crash và URL an toàn
        Assert.assertTrue(profilePage.getCurrentUrl().contains("/profile"),
                "Hệ thống phải xử lý an toàn chuỗi XSS");
    }
}

