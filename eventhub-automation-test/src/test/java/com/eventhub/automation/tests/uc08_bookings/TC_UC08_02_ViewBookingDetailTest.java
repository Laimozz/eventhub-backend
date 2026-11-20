package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingDetailPage;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_02_ViewBookingDetailTest extends BaseTest {

    @Test(description = "TC_UC08_02: Nhấn vào booking để xem đầy đủ thông tin chi tiết đơn hàng")
    public void testViewBookingDetail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        historyPage.clickFirstBookingDetail();

        BookingDetailPage detailPage = new BookingDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết booking phải được mở");
        Assert.assertFalse(detailPage.getBookingTitle().isEmpty(), "Tiêu đề trang chi tiết booking phải có mã đơn hàng");
        Assert.assertFalse(detailPage.getTotalAmount().isEmpty(), "Tổng tiền thanh toán phải được hiển thị");
    }
}

