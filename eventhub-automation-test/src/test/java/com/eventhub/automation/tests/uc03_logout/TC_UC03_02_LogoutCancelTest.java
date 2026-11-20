package com.eventhub.automation.tests.uc03_logout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC03_02_LogoutCancelTest extends BaseTest {

    @Test(description = "TC_UC03_02: Hủy bỏ đăng xuất khi người dùng chọn 'Hủy' trong hộp thoại xác nhận")
    public void testLogoutCancel() {
        // 1. Đăng nhập
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        Assert.assertTrue(header.isUserLoggedIn());

        // 2. Mở popup đăng xuất
        header.clickLogoutMenuItem();
        Assert.assertTrue(header.isLogoutModalDisplayed());

        // 3. Nhấn Hủy
        header.cancelLogout();

        // 4. Kiểm tra phiên đăng nhập vẫn được duy trì
        Assert.assertTrue(header.isUserLoggedIn(), "Phiên làm việc phải được giữ nguyên khi chọn Hủy");
    }
}

