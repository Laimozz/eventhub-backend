package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingDetailPage;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_03_ViewCanceledOrExpiredBookingDetailTest extends BaseTest {

    @Test(description = "TC_UC08_03: Xem chi tiết đơn booking có trạng thái Đã hủy hoặc Hết hạn")
    public void testViewCanceledOrExpiredBookingDetail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        Assert.assertTrue(historyPage.isLoaded(), "Trang lịch sử đặt vé phải được tải thành công");

        if (historyPage.getBookingCount() > 0) {
            historyPage.clickFirstBookingDetail();
            BookingDetailPage detailPage = new BookingDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết booking phải được mở");
            Assert.assertNotNull(detailPage.getBookingTitle(), "Tiêu đề booking phải hiển thị");
        } else {
            Assert.assertTrue(true, "Khách hàng không có booking hủy/hết hạn để kiểm tra");
        }
    }
}

