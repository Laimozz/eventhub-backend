package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_01_ViewBookingHistoryListTest extends BaseTest {

    @Test(description = "TC_UC08_01: Xem danh sách lịch sử đặt vé của khách hàng")
    public void testViewBookingHistoryList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();

        Assert.assertTrue(historyPage.getBookingCount() > 0, "Khách hàng phải có ít nhất 1 booking trong danh sách lịch sử");
    }
}

