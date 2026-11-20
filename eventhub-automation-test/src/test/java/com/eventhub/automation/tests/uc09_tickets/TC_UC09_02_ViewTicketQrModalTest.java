package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_02_ViewTicketQrModalTest extends BaseTest {

    @Test(description = "TC_UC09_02: Nhấn Phóng to mã QR hiển thị dialog mã QR kích thước lớn để check-in")
    public void testViewTicketQrModal() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        MyTicketsPage ticketsPage = new MyTicketsPage(getDriver()).open();
        ticketsPage.clickZoomFirstQr();

        Assert.assertTrue(ticketsPage.isQrDialogDisplayed(), "Hộp thoại phóng to mã QR phải được hiển thị");

        ticketsPage.closeQrDialog();
        Assert.assertFalse(ticketsPage.isQrDialogDisplayed(), "Hộp thoại phóng to mã QR phải đóng lại sau khi nhấn nút Đóng");
    }
}

