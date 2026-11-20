package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_02_UpdatePhoneSuccessTest extends BaseTest {

    @Test(description = "TC_UC04_02: Cập nhật thành công Số điện thoại với định dạng hợp lệ")
    public void testUpdatePhoneSuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        // Nhập số điện thoại hợp lệ mới
        String newPhone = "0378770331";
        profilePage.enterPhone(newPhone);
        profilePage.clickSubmit();

        // Kiểm tra thông báo thành công hoặc quay về màn hình xem hồ sơ
        Assert.assertTrue(profilePage.isToastOrSuccessDisplayed(), "Hệ thống phải hiển thị toast thông báo cập nhật thành công");
    }
}

