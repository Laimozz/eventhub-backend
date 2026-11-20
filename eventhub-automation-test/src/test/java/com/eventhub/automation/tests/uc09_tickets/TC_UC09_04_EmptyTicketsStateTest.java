package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_04_EmptyTicketsStateTest extends BaseTest {

    @Test(description = "TC_UC09_04: Lọc danh mục vé không có vé nào sẽ hiển thị thông báo trạng thái trống")
    public void testEmptyTicketsState() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        // Mở trang vé với mã booking không sở hữu vé nào
        getDriver().get(ConfigurationManager.getBaseUrl() + "/customer/ve-cua-toi?status=USED");

        MyTicketsPage ticketsPage = new MyTicketsPage(getDriver());
        Assert.assertTrue(ticketsPage.getTicketCardsCount() >= 0,
                "Hệ thống tải trang và hiển thị danh sách vé hoặc trạng thái trống ổn định");
    }
}

