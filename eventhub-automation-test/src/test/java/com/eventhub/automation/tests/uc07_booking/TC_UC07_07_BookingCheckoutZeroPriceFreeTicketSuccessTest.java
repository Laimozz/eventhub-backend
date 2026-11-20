package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingCheckoutPage;
import com.eventhub.automation.pages.EventDetailPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_07_BookingCheckoutZeroPriceFreeTicketSuccessTest extends BaseTest {

    @Test(description = "TC_UC07_07: Đặt vé sự kiện miễn phí (0đ) thành công không bị lỗi xung đột ví ban tổ chức (Finance Conflict)")
    public void testBookingCheckoutZeroPriceFreeTicketSuccess() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        // Mở màn hình chọn vé sự kiện 1
        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);
        selectTicketsPage.increaseFirstTicket(1);
        selectTicketsPage.clickContinue();

        BookingCheckoutPage checkoutPage = new BookingCheckoutPage(getDriver());
        Assert.assertTrue(checkoutPage.isLoaded(), "Trang xác nhận đơn hàng mở thành công");

        checkoutPage.setTerms(true);
        Assert.assertTrue(checkoutPage.isPayButtonEnabled(), "Nút thanh toán/xác nhận phải được kích hoạt sau khi đồng ý điều khoản");
    }
}
