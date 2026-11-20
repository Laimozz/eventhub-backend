package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingCheckoutPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_01_SelectTicketsAndProceedToCheckoutTest extends BaseTest {

    @Test(description = "TC_UC07_01: Đặt vé hợp lệ và chuyển tiếp thành công đến màn hình xác nhận thanh toán")
    public void testSelectTicketsAndProceedToCheckout() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);

        // Chọn 1 vé từ loại vé đầu tiên
        selectTicketsPage.increaseFirstTicket(1);
        Assert.assertTrue(selectTicketsPage.isContinueButtonEnabled(), "Nút Tiếp tục phải được kích hoạt khi đã chọn ít nhất 1 vé");

        selectTicketsPage.clickContinue();

        BookingCheckoutPage checkoutPage = new BookingCheckoutPage(getDriver());
        Assert.assertTrue(checkoutPage.isLoaded(), "Phải điều hướng thành công đến trang Xác nhận & thanh toán");
        Assert.assertFalse(checkoutPage.getTotalAmount().isEmpty(), "Tổng tiền thanh toán phải được hiển thị");
    }
}

