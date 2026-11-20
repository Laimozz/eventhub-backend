package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingCheckoutPage;
import com.eventhub.automation.pages.EventDetailPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_06_BookingCheckoutCountdownTimerTest extends BaseTest {

    @Test(description = "TC_UC07_06: Kiểm tra đồng hồ đếm ngược giữ chỗ (Countdown Timer) hiển thị tại trang xác nhận đặt vé")
    public void testBookingCheckoutCountdownTimer() {
        // Đăng nhập tài khoản Customer
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        // Mở màn hình chọn vé sự kiện 1
        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);
        selectTicketsPage.increaseFirstTicket(1);
        selectTicketsPage.clickContinue();

        BookingCheckoutPage checkoutPage = new BookingCheckoutPage(getDriver());
        Assert.assertTrue(checkoutPage.isLoaded(), "Trang xác nhận & thanh toán phải được hiển thị");
        Assert.assertTrue(checkoutPage.isCountdownTimerDisplayed(), "Đồng hồ đếm ngược giữ vé phải được hiển thị");
    }
}
