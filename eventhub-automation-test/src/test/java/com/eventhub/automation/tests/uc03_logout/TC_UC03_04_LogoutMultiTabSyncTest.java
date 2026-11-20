package com.eventhub.automation.tests.uc03_logout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.WindowType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC03_04_LogoutMultiTabSyncTest extends BaseTest {

    @Test(description = "TC_UC03_04: Đăng xuất trên tab này thì đồng bộ trạng thái đăng xuất trên tab khác")
    public void testLogoutMultiTabSync() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        Assert.assertTrue(header.isUserLoggedIn(), "Tài khoản phải ở trạng thái đăng nhập");

        String originalWindow = getDriver().getWindowHandle();

        // Mở Tab mới (Tab B)
        getDriver().switchTo().newWindow(WindowType.TAB);
        getDriver().get(ConfigurationManager.getBaseUrl() + "/customer/profile");
        String secondWindow = getDriver().getWindowHandle();

        // Quay lại Tab A để đăng xuất
        getDriver().switchTo().window(originalWindow);
        header.clickLogoutMenuItem();
        header.confirmLogout();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "Tab A phải chuyển về trang đăng nhập");

        // Chuyển sang Tab B và F5
        getDriver().switchTo().window(secondWindow);
        getDriver().navigate().refresh();

        boolean redirectedOrNoAuth = getDriver().getCurrentUrl().contains("/login")
                || !new CustomerHeaderComponent(getDriver()).isUserLoggedIn();

        Assert.assertTrue(redirectedOrNoAuth, "Tab B phải nhận diện phiên đăng nhập đã kết thúc");
    }
}
