package com.eventhub.automation.tests.uc03_logout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC03_01_LogoutSuccessTest extends BaseTest {

    @Test(description = "TC_UC03_01: Đăng xuất thành công khi người dùng xác nhận Đăng xuất trong hộp thoại")
    public void testLogoutSuccess() {
        // 1. Đăng nhập trước
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        // 2. Chờ vào giao diện sau đăng nhập
        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        Assert.assertTrue(header.isUserLoggedIn(), "Người dùng phải ở trạng thái đã đăng nhập");

        // 3. Thực hiện Đăng xuất
        header.clickLogoutMenuItem();
        Assert.assertTrue(header.isLogoutModalDisplayed(), "Hộp thoại xác nhận đăng xuất phải hiển thị");

        // 4. Nhấn Xác nhận
        header.confirmLogout();

        // 5. Kiểm tra chuyển hướng về trang /login hoặc header hiển thị nút Đăng nhập
        boolean isLoggedOut = header.isLoginButtonDisplayed() || header.getCurrentUrl().contains("/login");
        Assert.assertTrue(isLoggedOut, "Sau khi đăng xuất thành công phải quay về trạng thái chưa đăng nhập");
    }
}

