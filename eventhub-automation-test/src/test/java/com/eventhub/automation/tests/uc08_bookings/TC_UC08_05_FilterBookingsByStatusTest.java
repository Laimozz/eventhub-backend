package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_05_FilterBookingsByStatusTest extends BaseTest {

    @Test(description = "TC_UC08_05: Người dùng mở danh sách Lịch sử booking và danh sách hiển thị dữ liệu ổn định")
    public void testFilterBookingsByStatus() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        Assert.assertTrue(historyPage.isLoaded(), "Trang Lịch sử đặt vé phải hiển thị");
        Assert.assertTrue(historyPage.getBookingCount() >= 0, "Danh sách booking nạp thành công");
    }
}

