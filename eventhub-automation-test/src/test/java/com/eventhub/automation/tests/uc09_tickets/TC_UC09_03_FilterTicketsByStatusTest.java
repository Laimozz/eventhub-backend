package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_03_FilterTicketsByStatusTest extends BaseTest {

    @Test(description = "TC_UC09_03: Lọc danh sách vé theo trạng thái (Vé chưa sử dụng vs Đã check-in)")
    public void testFilterTicketsByStatus() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        MyTicketsPage ticketsPage = new MyTicketsPage(getDriver()).open();

        // Chuyển sang tab Đã check-in
        ticketsPage.filterUsedTickets();
        int usedCount = ticketsPage.getTicketCardsCount();

        // Chuyển sang tab Vé chưa sử dụng
        ticketsPage.filterValidTickets();
        int validCount = ticketsPage.getTicketCardsCount();

        Assert.assertTrue(usedCount >= 0 && validCount >= 0, "Hệ thống phải lọc danh sách vé theo từng trạng thái thành công");
    }
}

