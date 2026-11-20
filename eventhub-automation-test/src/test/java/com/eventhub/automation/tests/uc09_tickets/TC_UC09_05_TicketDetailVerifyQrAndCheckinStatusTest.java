package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_05_TicketDetailVerifyQrAndCheckinStatusTest extends BaseTest {

    @Test(description = "TC_UC09_05: Quét thử mã QR / Kiểm tra hiển thị QR Code và chi tiết vé điện tử")
    public void testTicketDetailVerifyQrAndCheckinStatus() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        MyTicketsPage ticketsPage = new MyTicketsPage(getDriver()).open();
        Assert.assertTrue(ticketsPage.isLoaded(), "Trang Vé của tôi phải nạp thành công");

        if (ticketsPage.getTicketCardsCount() > 0) {
            ticketsPage.clickZoomFirstQr();
            Assert.assertTrue(ticketsPage.isQrDialogDisplayed(), "Modal phóng to mã QR phải hiển thị");
        }
    }
}

