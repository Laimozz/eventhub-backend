package com.eventhub.automation.tests.uc04_profile;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC04_08_CheckRoleFieldReadOnlyTest extends BaseTest {

    @Test(description = "TC_UC04_08: Kiểm tra trường Vai trò (Role) ở chế độ chỉ đọc, không thể chỉnh sửa")
    public void testCheckRoleFieldReadOnly() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        ProfilePage profilePage = new ProfilePage(getDriver()).open();
        profilePage.clickEditProfile();

        Assert.assertTrue(profilePage.isRoleFieldReadOnly(), "Trường Vai trò tài khoản phải ở trạng thái Read-only/Disabled");
    }
}

