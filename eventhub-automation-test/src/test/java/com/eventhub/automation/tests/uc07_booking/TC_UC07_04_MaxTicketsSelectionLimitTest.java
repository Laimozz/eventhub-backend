package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_04_MaxTicketsSelectionLimitTest extends BaseTest {

    @Test(description = "TC_UC07_04: Kiểm tra giới hạn số lượng vé khi bấm tăng số lượng")
    public void testMaxTicketsSelectionLimit() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);

        // Bấm tăng vé 5 lần
        selectTicketsPage.increaseFirstTicket(5);

        Assert.assertTrue(selectTicketsPage.isContinueButtonEnabled(),
                "Nút tiếp tục phải khả dụng khi chọn vé trong giới hạn cho phép");
    }
}

