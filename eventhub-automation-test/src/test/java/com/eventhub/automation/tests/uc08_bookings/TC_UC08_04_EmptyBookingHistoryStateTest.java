package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_04_EmptyBookingHistoryStateTest extends BaseTest {

    @Test(description = "TC_UC08_04: Hiển thị trạng thái trống khi người dùng chưa từng đặt vé nào")
    public void testEmptyBookingHistoryState() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        // vol.customer1@eventhub.test chưa có đơn đặt vé nào trong cơ sở dữ liệu
        loginPage.login("vol.customer1@eventhub.test", ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        Assert.assertTrue(historyPage.isLoaded(), "Trang lịch sử đặt vé phải được tải thành công");

        // Khi người dùng chưa có booking, danh sách booking = 0
        Assert.assertEquals(historyPage.getBookingCount(), 0, "Người dùng mới chưa đặt vé phải có 0 đơn hàng hiển thị");
    }
}

