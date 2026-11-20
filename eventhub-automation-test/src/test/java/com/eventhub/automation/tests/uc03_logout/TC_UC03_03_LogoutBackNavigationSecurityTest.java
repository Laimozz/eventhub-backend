package com.eventhub.automation.tests.uc03_logout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC03_03_LogoutBackNavigationSecurityTest extends BaseTest {

    @Test(description = "TC_UC03_03: Kiểm tra bảo mật không thể truy cập lại trang riêng tư sau khi đăng xuất bằng nút Back của trình duyệt")
    public void testLogoutBackNavigationSecurity() {
        // 1. Đăng nhập
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        Assert.assertTrue(header.isUserLoggedIn());

        // 2. Vào trang cá nhân hoặc vé của tôi
        getDriver().get(ConfigurationManager.getBaseUrl() + "/customer/ve-cua-toi");

        // 3. Đăng xuất
        header.clickLogoutMenuItem();
        header.confirmLogout();

        // 4. Bấm nút Back trên trình duyệt
        getDriver().navigate().back();

        // 5. Kiểm tra hệ thống không cho phép xem dữ liệu riêng tư và chuyển về login
        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login") || header.isLoginButtonDisplayed(),
                "Không được xem lại dữ liệu sau khi đã đăng xuất");
    }
}

