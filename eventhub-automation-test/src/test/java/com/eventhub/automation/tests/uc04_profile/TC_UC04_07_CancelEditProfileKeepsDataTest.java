package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_07_CancelEditProfileKeepsDataTest extends BaseTest {

    @Test(description = "TC_UC04_07: Nhấn nút Hủy khi đang chỉnh sửa giữ nguyên thông tin cá nhân ban đầu")
    public void testCancelEditProfileKeepsData() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        String originalName = profilePage.getHeroFullName();

        profilePage.clickEditProfile();
        profilePage.enterFullName("Tên thay đổi tạm thời");
        profilePage.clickCancel();

        Assert.assertTrue(profilePage.getHeadlineTitle().contains("Thông tin cá nhân"),
                "Nhấn Hủy phải quay về màn hình xem thông tin cá nhân");
        Assert.assertEquals(profilePage.getHeroFullName(), originalName,
                "Họ tên phải giữ nguyên giá trị ban đầu khi hủy chỉnh sửa");
    }
}

