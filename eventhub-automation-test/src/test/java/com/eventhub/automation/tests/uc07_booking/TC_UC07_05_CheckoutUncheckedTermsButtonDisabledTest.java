package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingCheckoutPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_05_CheckoutUncheckedTermsButtonDisabledTest extends BaseTest {

    @Test(description = "TC_UC07_05: Tại màn hình thanh toán, nút thanh toán bị vô hiệu hóa khi chưa đồng ý điều khoản")
    public void testCheckoutUncheckedTermsButtonDisabled() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);
        selectTicketsPage.increaseFirstTicket(1);
        selectTicketsPage.clickContinue();

        BookingCheckoutPage checkoutPage = new BookingCheckoutPage(getDriver());
        Assert.assertTrue(checkoutPage.isLoaded(), "Trang thanh toán phải được hiển thị");

        // Đảm bảo checkbox điều khoản chưa được tick -> nút thanh toán bị disable
        checkoutPage.setTerms(false);
        Assert.assertFalse(checkoutPage.isPayButtonEnabled(), "Nút thanh toán phải bị disable khi chưa tick đồng ý điều khoản");

        // Tick checkbox điều khoản -> nút thanh toán được kích hoạt
        checkoutPage.setTerms(true);
        Assert.assertTrue(checkoutPage.isPayButtonEnabled(), "Nút thanh toán phải được kích hoạt sau khi tick đồng ý điều khoản");
    }
}

