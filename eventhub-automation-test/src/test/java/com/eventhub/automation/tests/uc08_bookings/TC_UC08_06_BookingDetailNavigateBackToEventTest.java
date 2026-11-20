package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingDetailPage;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_06_BookingDetailNavigateBackToEventTest extends BaseTest {

    @Test(description = "TC_UC08_06: Từ màn hình chi tiết booking có thể nhấn nút 'Xem sự kiện' để quay lại trang sự kiện")
    public void testBookingDetailNavigateBackToEvent() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        historyPage.clickFirstBookingDetail();

        BookingDetailPage detailPage = new BookingDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết booking phải được mở");

        detailPage.clickBackToEvent();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/events/"),
                "Phải điều hướng thành công về trang chi tiết sự kiện");
    }
}

