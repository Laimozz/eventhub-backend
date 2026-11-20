package com.eventhub.automation.tests.uc03_logout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CustomerHeaderComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC03_05_LogoutDropdownMenuDisplayTest extends BaseTest {

    @Test(description = "TC_UC03_05: Kiểm tra giao diện Menu Dropdown khi click vào nút Tài khoản")
    public void testDropdownMenuDisplay() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        CustomerHeaderComponent header = new CustomerHeaderComponent(getDriver());
        header.openAccountMenu();

        Assert.assertTrue(header.isAccountDropdownDisplayed(), "Dropdown menu phải hiển thị");
        Assert.assertTrue(header.isProfileItemDisplayed(), "Mục Thông tin cá nhân phải hiển thị");
        Assert.assertTrue(header.isMyBookingsItemDisplayed(), "Mục Booking của tôi phải hiển thị");
        Assert.assertTrue(header.isMyTicketsItemDisplayed(), "Mục Vé của tôi phải hiển thị");
    }
}

