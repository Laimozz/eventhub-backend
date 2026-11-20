package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_01_ViewMyTicketsListTest extends BaseTest {

    @Test(description = "TC_UC09_01: Khách hàng xem danh sách vé điện tử đã mua")
    public void testViewMyTicketsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        MyTicketsPage ticketsPage = new MyTicketsPage(getDriver()).open();

        Assert.assertTrue(ticketsPage.getTicketCardsCount() > 0, "Danh sách vé điện tử của khách hàng phải có ít nhất 1 vé");
    }
}

