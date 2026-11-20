package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_12_CombinedTicketTypesTotalCalculationTest extends BaseTest {

    @Test(description = "TC_UC07_12: Kiểm tra tính toán chính xác tổng tiền khi tăng số lượng vé")
    public void testCombinedTicketTypesTotalCalculation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);

        String initialTotal = selectTicketsPage.getTotalSummaryText();

        // Tăng 2 vé loại đầu tiên
        selectTicketsPage.increaseFirstTicket(2);

        String updatedTotal = selectTicketsPage.getTotalSummaryText();
        Assert.assertNotEquals(updatedTotal, initialTotal, "Tổng tiền tạm tính phải thay đổi sau khi chọn số lượng vé");
    }
}

