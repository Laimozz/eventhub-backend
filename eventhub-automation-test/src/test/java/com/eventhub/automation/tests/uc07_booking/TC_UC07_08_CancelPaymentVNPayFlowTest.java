package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingCheckoutPage;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_08_CancelPaymentVNPayFlowTest extends BaseTest {

    @Test(description = "TC_UC07_08: Khách hàng hủy thao tác thanh toán từ màn hình checkout và quay về lịch sử đơn hàng")
    public void testCancelPaymentVNPayFlow() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(41);
        selectTicketsPage.increaseFirstTicket(1);
        selectTicketsPage.clickContinue();

        BookingCheckoutPage checkoutPage = new BookingCheckoutPage(getDriver());
        Assert.assertTrue(checkoutPage.isLoaded(), "Trang xác nhận đơn hàng thanh toán phải được hiển thị");

        checkoutPage.clickBackToHistory();

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver());
        Assert.assertTrue(historyPage.isLoaded(), "Khách hàng phải quay trở lại trang Lịch sử booking an toàn");
    }
}

